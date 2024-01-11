package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;

import java.util.Objects;

/**
 * Contains all context information needed by the decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class DecoderContext {
    private final DecoderService decoderService;
    private final Gestalt gestalt;

    public DecoderContext(DecoderService decoderService, Gestalt gestalt) {
        this.decoderService = decoderService;
        this.gestalt = gestalt;
    }

    public DecoderService getDecoderService() {
        return decoderService;
    }

    public Gestalt getGestalt() {
        return gestalt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DecoderContext)) {
            return false;
        }
        DecoderContext that = (DecoderContext) o;
        return Objects.equals(decoderService, that.decoderService) && Objects.equals(gestalt, that.gestalt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decoderService, gestalt);
    }
}
