package au.edu.federation.caliko.utils;

import java.io.*;

import au.edu.federation.caliko.FabrikChain;
import au.edu.federation.caliko.FabrikStructure;

/**
 * Utility class to serialize and de-serialize IK structures and chains.
 *
 * @author alansley - 19/06/2019
 */
public final class SerializationUtil {

    private SerializationUtil() {
    }

    public static <T extends FabrikChain<?, ?, ?, ?>> void serializeChain(final T chain, final File outputFile) throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            outputStream.writeObject(chain);
            outputStream.flush();
        }
    }

    /**
     * Serialized a FabrikChain to an OutputStream
     *
     * @param <T>       The type of FabrikChain.
     * @param chain     The FabrikChain to serialize.
     * @param output    The FileOutputStream to write the serialized data to.
     * @throws IOException Any problem writing the object to fail, and we throw an exception.
     */
    public static <T extends FabrikChain<?, ?, ?, ?>> void serializeChain(final T chain, final FileOutputStream output) throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(output)) {
            outputStream.writeObject(chain);
            outputStream.flush();
        }
    }

    public static <T extends FabrikChain<?, ?, ?, ?>> T deserializeChain(final File inputFile, final Class<T> clazz) throws IOException, ClassNotFoundException {
        return deserializeChain(new FileInputStream(inputFile), clazz);
    }

    /**
     * Deserializes a FabrikChain in binary format from an InputStream
     *
     * @param <T>   The type of FabrikChain.
     * @param input The InputStream of the binary file to deserialize.
     * @param clazz The type of FabrikChain that must be deserialized.
     * @return The FabrikChain unmarshalled from the InputStream.
     * @throws Exception Any problem deserializing the FabrikChain and we bail.
     */
    public static <T extends FabrikChain<?, ?, ?, ?>> T deserializeChain(final InputStream input, final Class<T> clazz) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(input);
        Object object = in.readObject();

        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        else {
            throw new ClassCastException("Can't cast read object to " + clazz.getSimpleName());
        }
    }

    /**
     * Marshalls a FabrikStructure as XML to an OutputStream
     *
     * @param <T>       The type of FabrikStructure.
     * @param structure The FabrikStructure to serialize.
     * @param fos       The FileOutputStream to write out the serialized data to.
     * @throws IOException Any problem marshalling the FabrikStructure and we bail.
     */
    public static <T extends FabrikStructure<?, ?>> void serializeStructure(final T structure, final FileOutputStream fos) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(fos)) {
            output.writeObject(structure);
            output.flush();
        }
    }

    /**
     * Deserializes a FabrikStructure in binary form from an InputStream
     *
     * @param <T>   The type of FabrikStructure.
     * @param input    The InputStream of the binary data to deserialize from.
     * @param clazz The type of FabrikStructure that must be unmarshalled
     * @return The FabrikStructure deserialized from the FileInputStream.
     * @throws IOException Any problem unmarshalling the FabrikStructure and we bail.
     */
    public static <T extends FabrikStructure<?, ?>> T deserializeStructure(final InputStream input, Class<T> clazz) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(input);
        Object object = in.readObject();

        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        else {
            throw new ClassCastException("Can't cast read object to " + clazz.getSimpleName());
        }
    }

}
