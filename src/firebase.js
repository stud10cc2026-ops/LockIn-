import { initializeApp } from "firebase/app";
import {
  getAuth,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendEmailVerification,
  signOut
} from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyCFByIoLPk96jDQt9QGQnRlk-tEaK0SE0o",
  authDomain: "lock-in-e6bed.firebaseapp.com",
  projectId: "lock-in-e6bed",
  storageBucket: "lock-in-e6bed.firebasestorage.app",
  messagingSenderId: "667742735742",
  appId: "1:667742735742:web:d383968e1613208a12772f"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);

export async function loginUser(email, password) {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    if (!user.emailVerified) {
      await sendEmailVerification(user);
      await signOut(auth);
      return {
        user: null,
        isUnverified: true,
        error: `We have sent you a verification email to ${email}. Please verify it and log in.`
      };
    }
    return { user: userCredential.user, error: null };
  } catch (error) {
    return { user: null, error: "Email or password is incorrect" };
  }
}

export async function registerUser(email, password) {
  try {
    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    await sendEmailVerification(userCredential.user);
    await signOut(auth);
    return {
      user: null,
      isUnverified: true,
      message: `We have sent you a verification email to ${email}. Please verify it and log in.`
    };
  } catch (error) {
    if (error.code === "auth/email-already-in-use") {
      return { user: null, error: "User already exists. Please sign in" };
    }
    return { user: null, error: "Email or password is incorrect" };
  }
}

export async function logoutUser() {
  await signOut(auth);
}
