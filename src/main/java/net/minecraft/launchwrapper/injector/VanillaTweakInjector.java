package net.minecraft.launchwrapper.injector;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.imageio.ImageIO;
import java.io.File;

import static org.objectweb.asm.Opcodes.*;

public class VanillaTweakInjector implements IClassTransformer {
    public VanillaTweakInjector() {
    }

    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (!"net.minecraft.client.Minecraft".equals(name)) {
            return bytes;
        }

        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode mainMethod = null;
        for (final MethodNode methodNode : classNode.methods) {
            if ("main".equals(methodNode.name)) {
                mainMethod = methodNode;
                break;
            }
        }
        if (mainMethod == null) {
            // WTF? We got no main method
            return bytes;
        }

        FieldNode workDirNode = null;
        for (final FieldNode fieldNode : classNode.fields) {
            final String fileTypeDescriptor = Type.getDescriptor(File.class);
            if (fileTypeDescriptor.equals(fieldNode.desc) && (fieldNode.access & ACC_STATIC) == ACC_STATIC) {
                workDirNode = fieldNode;
                break;
            }
        }

        // Prepare our injection code
        final MethodNode injectedMethod = new MethodNode();
        final Label label = new Label();
        injectedMethod.visitLabel(label);
        injectedMethod.visitLineNumber(9001, label); // Linenumber which shows up in the stacktrace
        // Call the method below
        injectedMethod.visitMethodInsn(INVOKESTATIC, "net/minecraft/launchwrapper/injector/VanillaTweakInjector", "inject", "()Ljava/io/File;");
        // Store the result in the workDir variable.
        injectedMethod.visitFieldInsn(PUTSTATIC, "net/minecraft/client/Minecraft", workDirNode.name, "Ljava/io/File;");

        mainMethod.instructions.insert(injectedMethod.instructions);

        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static File inject() {
        // Speed up imageloading
        System.out.println("Turning off ImageIO disk-caching");
        ImageIO.setUseCache(false);

        // Set the workdir, return value will get assigned
        System.out.println("Setting gameDir to: " + Launch.minecraftHome);
        return Launch.minecraftHome;
    }

}
