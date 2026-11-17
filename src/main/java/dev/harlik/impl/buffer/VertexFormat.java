package dev.harlik.impl.buffer;

import com.mojang.blaze3d.GpuFormat;
import java.util.LinkedHashMap;

public record VertexFormat(LinkedHashMap<String, Integer> attributes) {

    /** Builds the matching Blaze3D vertex format. Attribute names must match the shader inputs. */
    public com.mojang.blaze3d.vertex.VertexFormat gpu() {
        var builder = com.mojang.blaze3d.vertex.VertexFormat.builder(0);
        attributes.forEach((name, size) -> builder.addAttribute(name, switch (size) {
            case 1 -> GpuFormat.R32_FLOAT;
            case 2 -> GpuFormat.RG32_FLOAT;
            case 3 -> GpuFormat.RGB32_FLOAT;
            case 4 -> GpuFormat.RGBA32_FLOAT;
            default -> throw new IllegalArgumentException("Unsupported attribute size " + size + " for " + name);
        }));
        return builder.build();
    }

    public int getStride() {
        int total = 0;
        for (Integer i : attributes.values()) {
            total += i * Float.BYTES;
        }
        return total;
    }

    public int getFloats() {
        int total = 0;
        for (Integer i : attributes.values()) {
            total += i;
        }
        return total;
    }

    public static final VertexFormat RECTANGLE = new VertexFormat(
            new LinkedHashMap<String, Integer>() {{
                put("aPos", 2);
                put("aSize", 2);
                put("aColor", 4);
                put("aRadius", 4);
                put("aSmooth", 1);
                put("aUV", 2);
            }}
    );

    public static final VertexFormat GLASS = new VertexFormat(
            new LinkedHashMap<String, Integer>() {{
                put("aPos", 2);
                put("aSize", 2);
                put("aColor", 4);
                put("aRadius", 4);
                put("aSmooth", 1);
                put("aUV", 2);
                put("aGlass", 4);
                put("aHighlight", 1);
            }}
    );

    public static final VertexFormat TEXT =new VertexFormat(
            new LinkedHashMap<String, Integer>() {{
                put("aPos", 2);
                put("aUV", 2);
                put("aColor", 4);
                put("aPxRange", 1);
            }}
    );
}
