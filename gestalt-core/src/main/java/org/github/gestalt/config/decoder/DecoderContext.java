package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.secret.rules.SecretConcealer;

import java.util.Objects;

/**
 * Contains all context information needed by the decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class DecoderContext {
    private final DecoderService decoderService;
    private final Gestalt gestalt;
    private final SentenceLexer defaultLexer;
    private final SecretConcealer secretConcealer;

    public DecoderContext(DecoderService decoderService, Gestalt gestalt, SecretConcealer secretConcealer, SentenceLexer defaultLexer) {
        this.decoderService = decoderService;
        this.gestalt = gestalt;
        this.secretConcealer = secretConcealer;
        this.defaultLexer = defaultLexer;
    }

    public DecoderService getDecoderService() {
        return decoderService;
    }

    public Gestalt getGestalt() {
        return gestalt;
    }

    public SecretConcealer getSecretConcealer() {
        return secretConcealer;
    }

    public SentenceLexer getDefaultLexer() {
        return defaultLexer;
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
        return Objects.equals(decoderService, that.decoderService) &&
            Objects.equals(gestalt, that.gestalt) &&
            Objects.equals(secretConcealer, that.secretConcealer) &&
            Objects.equals(defaultLexer, that.defaultLexer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decoderService, gestalt, secretConcealer, defaultLexer);
    }
}
