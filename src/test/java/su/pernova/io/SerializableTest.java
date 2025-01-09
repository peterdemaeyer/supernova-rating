package su.pernova.io;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Test;

public interface SerializableTest {

	Serializable newInstance();

	@Test
	default void serialization() throws Exception {
			final Serializable object = newInstance();
			final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			try (final ObjectOutputStream objectOut = new ObjectOutputStream(byteArrayOut)) {
				objectOut.writeObject(object);
			}
			final ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(byteArrayOut.toByteArray());
			Object deserializedObject;
			try (final ObjectInputStream objectIn = new ObjectInputStream(byteArrayIn)) {
				deserializedObject = objectIn.readObject();
			}
			assertInstanceOf(object.getClass(), deserializedObject);
	}
}
