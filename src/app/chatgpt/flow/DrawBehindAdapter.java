package app.chatgpt.flow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

interface DrawBehindAdapter {
    Object append(Object modifier, Object callback) throws ReflectiveOperationException;

    static DrawBehindAdapter resolve(ComposeSymbols symbols, Class<?> modifier, Class<?> callback)
            throws ReflectiveOperationException {
        try {
            Method factory = symbols.method("cja0", "x", modifier, callback);
            if (!Modifier.isStatic(factory.getModifiers()) || factory.getReturnType() != modifier) {
                throw new NoSuchMethodException("cja0.x must be a static Modifier factory");
            }
            factory.setAccessible(true);
            return (original, draw) -> factory.invoke(null, original, draw);
        } catch (ClassNotFoundException | NoSuchMethodException missingFactory) {
            try {
                // The verified cja0.x body is exactly original.C(new o500(draw)).
                Class<?> element = symbols.type("o500");
                if (!modifier.isAssignableFrom(element)) {
                    throw new NoSuchMethodException("o500 is not a Modifier element");
                }
                Constructor<?> constructor = element.getDeclaredConstructor(callback);
                Method then = modifier.getMethod("C", modifier);
                if (Modifier.isStatic(then.getModifiers()) || then.getReturnType() != modifier) {
                    throw new NoSuchMethodException("g5i0.C must append a Modifier");
                }
                constructor.setAccessible(true);
                then.setAccessible(true);
                return (original, draw) -> then.invoke(original, constructor.newInstance(draw));
            } catch (ReflectiveOperationException missingElement) {
                NoSuchMethodException unsupported = new NoSuchMethodException(
                        "DrawBehind unavailable: cja0.x and the verified o500/g5i0.C fallback are incompatible");
                unsupported.initCause(missingFactory);
                unsupported.addSuppressed(missingElement);
                throw unsupported;
            }
        }
    }
}
