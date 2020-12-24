package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.utils.ValidateOf;
import org.config.gestalt.GestaltCore;
import org.config.gestalt.node.ConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DecoderRegistry implements DecoderService {
    private static final Logger logger = LoggerFactory.getLogger(GestaltCore.class.getName());

    private List<Decoder> decoders = new ArrayList<>();

    public DecoderRegistry(List<Decoder> decoders) throws ConfigurationException {
        if (decoders != null) {
            this.decoders.addAll(decoders);
        } else {
            throw new ConfigurationException("Decoder list was null");
        }
    }

    @Override
    public void addDecoders(List<Decoder> addDecoders) {
        decoders.addAll(addDecoders);
    }

    @Override
    public List<Decoder> getDecoders() {
        return decoders;
    }

    @Override
    public void setDecoders(List<Decoder> decoders) {
        this.decoders = decoders;
    }

    <T> List<Decoder> getDecoderForClass(TypeCapture<T> klass) {
        return decoders.stream().filter(decoder -> decoder.matches(klass)).collect(Collectors.toList());
    }

    @Override
    public <T> ValidateOf<T> decodeNode(String path, ConfigNode configNode, TypeCapture<T> klass) {
        List<Decoder> classDecoder = getDecoderForClass(klass);

        if (classDecoder.isEmpty()) {
            return ValidateOf.inValid(new ValidationError.NoDecodersFound(klass.getName()));
        } else if (classDecoder.size() > 1) {
            logger.warn("Found multiple decoders for {}, found: {}", klass, classDecoder);
        }

        return classDecoder.get(0).decode(path, configNode, klass, this);
    }
}
