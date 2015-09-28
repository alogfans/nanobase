package com.alogfans.nanobase.rpc.serialize;

import java.io.*;

/**
 * Java Built-in Serializer. All are static methods.
 *
 * Created by Alogfans on 2015/9/27.
 */
public class BuiltInSerializer {
    /**
     * Convert serialized object to bytes using ObjectOutputStream
     * @param object the object for serialization, should be serialized.
     * @return the corresponding byte flows.
     * @throws Exception
     */
    public static byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(object);
        objectStream.flush();

        byte[] bytes = byteStream.toByteArray();

        objectStream.close();
        byteStream.close();

        return bytes;
    }

    /**
     * Convert bytes to raw object with correct type using ObjectInputStream
     * @param bytes byte flows for deserialization.
     * @return the corresponding object.
     * @throws Exception (ClassNotFoundException or IOException)
     */
    public static Object deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);

        Object object = objectStream.readObject();

        objectStream.close();
        byteStream.close();

        return object;
    }
}
