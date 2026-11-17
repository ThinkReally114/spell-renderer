package dev.harlik.impl.gpu;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.harlik.SpellRenderer;
import dev.harlik.impl.buffer.VertexFormat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

/**
 * Every render pipeline spell-renderer draws with. A pipeline bundles the shaders with the fixed state
 * (blend, cull, depth, vertex layout, bindings), so the same description works on the OpenGL and the Vulkan backend.
 * Shader sources live in {@code assets/spell-renderer/shaders/core/}.
 */
public final class SpellPipelines {

    /** Uniform block holding the orthographic projection, see {@code rectangle.vsh}. */
    public static final String PROJECTION_BLOCK = "SpellProjection";
    /** Uniform block with the glass constants, see {@code glass.fsh}. */
    public static final String GLASS_BLOCK = "SpellGlass";
    /** Uniform block with the source texel size of one Kawase downsample, see {@code kawase_down.fsh}. */
    public static final String KAWASE_BLOCK = "SpellKawase";

    public static final String TEXTURE_SAMPLER = "uTexture";
    public static final String ATLAS_SAMPLER = "uAtlas";
    public static final String BACKGROUND_SAMPLER = "uBackground";
    public static final String SOURCE_SAMPLER = "uSource";

    private static final BindGroupLayout PROJECTION_LAYOUT = BindGroupLayout.builder()
            .withUniform(PROJECTION_BLOCK, UniformType.UNIFORM_BUFFER)
            .build();
    private static final BindGroupLayout TEXTURE_LAYOUT = BindGroupLayout.builder()
            .withSampler(TEXTURE_SAMPLER)
            .build();
    private static final BindGroupLayout ATLAS_LAYOUT = BindGroupLayout.builder()
            .withSampler(ATLAS_SAMPLER)
            .build();
    private static final BindGroupLayout GLASS_LAYOUT = BindGroupLayout.builder()
            .withSampler(BACKGROUND_SAMPLER)
            .withUniform(GLASS_BLOCK, UniformType.UNIFORM_BUFFER)
            .build();
    private static final BindGroupLayout KAWASE_LAYOUT = BindGroupLayout.builder()
            .withSampler(SOURCE_SAMPLER)
            .withUniform(KAWASE_BLOCK, UniformType.UNIFORM_BUFFER)
            .build();

    /** glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA) for color and alpha alike. BlendFunction.TRANSLUCENT blends alpha differently. */
    private static final BlendFunction ALPHA_BLEND = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);

    private static final RenderPipeline.Snippet SHAPE = RenderPipeline.builder()
            .withBindGroupLayout(PROJECTION_LAYOUT)
            .withColorTargetState(new ColorTargetState(ALPHA_BLEND))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .buildSnippet();

    public static final RenderPipeline RECTANGLE = RenderPipeline.builder(SHAPE)
            .withLocation(id("pipeline/rectangle"))
            .withVertexShader(id("core/rectangle"))
            .withFragmentShader(id("core/rectangle"))
            .withVertexBinding(0, VertexFormat.RECTANGLE.gpu())
            .build();

    public static final RenderPipeline IMAGE = RenderPipeline.builder(SHAPE)
            .withLocation(id("pipeline/image"))
            .withVertexShader(id("core/rectangle"))
            .withFragmentShader(id("core/image"))
            .withBindGroupLayout(TEXTURE_LAYOUT)
            .withVertexBinding(0, VertexFormat.RECTANGLE.gpu())
            .build();

    public static final RenderPipeline TEXT = RenderPipeline.builder(SHAPE)
            .withLocation(id("pipeline/text"))
            .withVertexShader(id("core/text"))
            .withFragmentShader(id("core/text"))
            .withBindGroupLayout(ATLAS_LAYOUT)
            .withVertexBinding(0, VertexFormat.TEXT.gpu())
            .build();

    public static final RenderPipeline GLASS = RenderPipeline.builder(SHAPE)
            .withLocation(id("pipeline/glass"))
            .withVertexShader(id("core/glass"))
            .withFragmentShader(id("core/glass"))
            .withBindGroupLayout(GLASS_LAYOUT)
            .withVertexBinding(0, VertexFormat.GLASS.gpu())
            .build();

    /** Fullscreen triangle from gl_VertexID, no vertex buffer and no blending. */
    public static final RenderPipeline KAWASE_DOWN = RenderPipeline.builder()
            .withLocation(id("pipeline/kawase_down"))
            .withVertexShader(id("core/blur"))
            .withFragmentShader(id("core/kawase_down"))
            .withBindGroupLayout(KAWASE_LAYOUT)
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();

    private static final List<RenderPipeline> ALL = List.of(RECTANGLE, IMAGE, TEXT, GLASS, KAWASE_DOWN);

    private SpellPipelines() {}

    /**
     * Compiles every pipeline up front so shader errors show up in the log at startup instead of on first draw.
     * Sources are read straight from the resource manager, because Minecraft's shader cache is only filled
     * once the first resource reload finishes. After a reload Minecraft drops the cache and recompiles lazily.
     */
    public static void precompileAll() {
        ShaderSource source = (location, type) -> {
            Identifier file = type.idConverter().idToFile(location);
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
            if (resource.isEmpty()) {
                SpellRenderer.LOGGER.error("Missing shader {}", file);
                return null;
            }
            try (var stream = resource.get().open()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                SpellRenderer.LOGGER.error("Failed to read shader {}: {}", file, e.toString());
                return null;
            }
        };

        for (RenderPipeline pipeline : ALL) {
            if (!RenderSystem.getDevice().precompilePipeline(pipeline, source).isValid()) {
                SpellRenderer.LOGGER.error("Failed to compile pipeline {}", pipeline.getLocation());
            }
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SpellRenderer.MOD_ID, path);
    }
}
