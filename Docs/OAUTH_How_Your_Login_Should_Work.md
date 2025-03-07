### **✅ How Your Login Should Work (Step-by-Step)**
We are using **OAuth2 Authorization Code Flow + OIDC + Refresh Tokens**, which means:

1️⃣ **User enters the site (`http://localhost:3000/`)**  
   - If **a valid JWT exists in cookies**, authenticate **without redirecting to Google**.
   - If **only a refresh token exists**, request a new access token **without redirecting to Google**.
   - If **no valid JWT or refresh token exists**, the user must **manually press "Login"**.

2️⃣ **User clicks "Login"**  
   - Redirect to **Google OAuth login** (`https://accounts.google.com/o/oauth2/auth`).
   - User logs in with Google.
   - Google **redirects back to your backend** (`http://localhost:8080/login/oauth2/code/google`).
   - Backend:
     - Validates the **OAuth2 Code** and exchanges it for an **ID Token + Access Token**.
     - Creates a **refresh token** (stored in a **HttpOnly cookie**).
     - Redirects the user to `http://localhost:3000/login-success`.

3️⃣ **Frontend Handles Login Success (`/login-success`)**  
   - Fetch **user data** (`GET /api/users/me`).
   - If authenticated, **redirect to dashboard** (`/dashboard`).
   - If not authenticated, **redirect to homepage (`/`)**.

---

## **🔹 What You Need to Implement**
1. **A Login Page (`/login`)** → User clicks "Login"
2. **Handle `/login-success` to fetch user info** (so login completes)
3. **Auto-login using JWT or refresh token** (if available)

---

## **🛠 1️⃣ Implement the Login Page (`/login`)**
Create a new page in your React app:  

🔹 **`src/pages/Login.tsx`**
```tsx
import { useEffect } from "react";
import { useAuth } from "../hooks/useAuth";

export default function Login() {
  const { login } = useAuth();

  useEffect(() => {
    // If the user manually enters /login, redirect to OAuth login
    login();
  }, []);

  return (
    <div>
      <h2>Redirecting to Login...</h2>
    </div>
  );
}
```
### **🔹 What this does:**
- When a user **enters `/login`**, it **automatically calls `login()`**.
- `login()` redirects the user to Google OAuth.
- There is **no login form**, since Google handles authentication.

---

## **🛠 2️⃣ Modify `App.tsx` to Include the Login Page**
Update **React Router** to include the new `/login` page:

🔹 **`src/App.tsx`**
```tsx
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import { useAuth } from "./hooks/useAuth";

export default function App() {
  const { user } = useAuth();

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />  {/* ✅ Login Page */}
        <Route path="/dashboard" element={user ? <Dashboard /> : <Navigate to="/login" />} />
        <Route path="/login-success" element={<Navigate to="/dashboard" />} /> {/* ✅ Redirect after login */}
      </Routes>
    </Router>
  );
}
```
### **🔹 What this does:**
- If **not logged in**, users will **be sent to `/login`**.
- After login, they will be **redirected to `/dashboard`**.
- If `/login-success` is hit, **it redirects to `/dashboard`**.

---

## **🛠 3️⃣ Fix `useAuth.ts` to Handle JWT & Refresh Tokens**
🔹 **`src/hooks/useAuth.ts`**
```tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import { User } from "../types/user";

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuthentication = async () => {
      try {
        const response = await api.get("/api/users/me");  // ✅ Fetch user if JWT exists
        setUser(response.data);
      } catch (error) {
        console.error("User not authenticated:", error);
        setUser(null);
      }
    };

    checkAuthentication();  // ✅ Check authentication on load
  }, []);

  const login = () => {
    window.location.href = `${api.defaults.baseURL}/oauth2/authorization/google`; // ✅ Redirect to Google
  };

  const logout = async () => {
    await api.get("/auth/logout");
    setUser(null);
    navigate("/");
  };

  return { user, login, logout };
}
```
### **🔹 What this does:**
- **On page load**, it tries to fetch the user:
  - ✅ If JWT is valid → User is set
  - ❌ If JWT is missing or expired → No user (must log in)
- **Login** → Redirects to **Google OAuth**.
- **Logout** → Clears session and redirects.

---

## **🛠 4️⃣ Handle Login Success (`/login-success`)**
After Google login, the user is redirected to `/login-success`.  
This should **fetch the user data and move to the dashboard**.

🔹 **`src/pages/LoginSuccess.tsx`**
```tsx
import { useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function LoginSuccess() {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      navigate("/dashboard"); // ✅ Redirect once user data is loaded
    }
  }, [user, navigate]);

  return <h2>Logging in...</h2>;
}
```
### **🔹 What this does:**
- When the user **lands on `/login-success`**, it checks authentication.
- If authenticated, **redirect to `/dashboard`**.
- If not, it waits until authentication completes.

---

## **🔹 Final Overview**
| **Action** | **What Happens** |
|------------|----------------|
| **User enters the site** (`/`) | ✅ If JWT exists → Auto-login. <br> ❌ If no JWT → Show login button. |
| **User clicks "Login"** | 🔀 Redirect to Google OAuth. |
| **Google Redirects to Backend (`/login/oauth2/code/google`)** | 🖥️ Backend validates OAuth2 code, sets cookies, and redirects to `/login-success`. |
| **User lands on `/login-success`** | 🔄 Fetches user data → Redirect to `/dashboard`. |
| **User lands on `/dashboard`** | 🎉 Displays user info (if authenticated). |

---

## **🚀 Next Steps**
1. ✅ **Implement the changes** (Login page, authentication flow, React Router updates).
2. ✅ **Test Login & Logout:**
   - Check if JWT is used properly.
   - Try logging in and out multiple times.
   - Ensure refresh token logic works.
3. 🔄 **Check Edge Cases:**
   - If the JWT is **expired**, does it request a refresh token?
   - What happens if the user **deletes cookies**?

---

### **🔹 Final Thoughts**
- The **login should happen only when the user presses the button**, **not automatically**.  
- If a **valid JWT or refresh token exists**, login should happen **without user interaction**.  
- **Google handles authentication**, but your app needs to **fetch user data after login**.

Try these fixes, and let me know how it works! 🚀