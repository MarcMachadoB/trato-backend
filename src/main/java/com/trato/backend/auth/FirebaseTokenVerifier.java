package com.trato.backend.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenVerifier {

    private final ObjectProvider<FirebaseAuth> firebaseAuthProvider;

    public FirebaseTokenVerifier(ObjectProvider<FirebaseAuth> firebaseAuthProvider) {
        this.firebaseAuthProvider = firebaseAuthProvider;
    }

    public FirebaseToken verify(String idToken) throws FirebaseAuthException {
        try {
            return firebaseAuthProvider.getObject().verifyIdToken(idToken);
        } catch (BeansException | IllegalStateException exception) {
            throw new FirebaseAdminUnavailableException(
                    "Firebase Admin no está configurado o no está disponible.",
                    exception);
        }
    }
}
