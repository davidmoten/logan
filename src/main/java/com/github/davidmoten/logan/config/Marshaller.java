package com.github.davidmoten.logan.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Marshaller for {@link Configuration}.
 * 
 */
public class Marshaller {

    public static final String NAMESPACE = "http://github.com/davidmoten/logan/configuration";

    private final javax.xml.bind.Marshaller marshaller;

    private final Unmarshaller unmarshaller;

    /**
     * Constructor.
     */
    public Marshaller() {
        try {
            JAXBContext jc = JAXBContext.newInstance(Configuration.class);
            marshaller = jc.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            unmarshaller = jc.createUnmarshaller();
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Marshals {@link Configuration} to xml.
     * 
     * @param configuration
     *            configuration
     * @param os
     *            output stream
     */
    public synchronized void marshal(Configuration configuration, OutputStream os) {
        try {
            JAXBElement<Configuration> element = new JAXBElement<Configuration>(
                    new QName(NAMESPACE, "configuration"), Configuration.class, configuration);
            marshaller.marshal(element, os);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized String marshal(Configuration configuration) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        marshal(configuration, bytes);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    /**
     * Unmarshals xml to {@link Configuration}.
     * 
     * @param is
     *            input stream
     * @return configuration
     */
    public synchronized Configuration unmarshal(InputStream is) {
        StreamSource xml = new StreamSource(is);
        JAXBElement<Configuration> element;
        try {
            element = unmarshaller.unmarshal(xml, Configuration.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return element.getValue();
    }

}
