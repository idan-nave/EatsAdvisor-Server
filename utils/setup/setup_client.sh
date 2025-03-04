#!/bin/bash

# Exit on error
set -e

# Define project name
PROJECT_NAME="Client"

# Step 1: Create Vite project with React and TypeScript
echo "Initializing Vite project..."
npm create vite@latest "$PROJECT_NAME" -- --template react-ts

# Change into project directory
cd "$PROJECT_NAME"

# Step 2: Install dependencies
echo "Installing dependencies..."
npm install

# Install additional essential dependencies
echo "Installing React Router, React Hook Form, and Zod..."
npm install react-router-dom react-hook-form zod axios

# Install development dependencies
echo "Installing ESLint, Prettier, and TypeScript plugins..."
npm install -D eslint prettier eslint-plugin-react eslint-config-prettier eslint-plugin-import @typescript-eslint/parser @typescript-eslint/eslint-plugin vite-tsconfig-paths

# Step 3: Configure ESLint & Prettier
echo "Setting up ESLint and Prettier..."
cat > .eslintrc.json <<EOL
{
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:@typescript-eslint/recommended",
    "prettier"
  ],
  "parser": "@typescript-eslint/parser",
  "plugins": ["react", "@typescript-eslint"],
  "rules": {
    "react/react-in-jsx-scope": "off"
  },
  "settings": {
    "react": {
      "version": "detect"
    }
  }
}
EOL

cat > .prettierrc.json <<EOL
{
  "semi": false,
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 80,
  "tabWidth": 2
}
EOL

# Step 4: Create recommended folder structure
echo "Setting up folder structure..."
mkdir -p src/{assets,components,hooks,pages,services,types,utils}

# Create example files
echo "Creating example files..."

# Basic app structure
cat > src/main.tsx <<EOL
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import { BrowserRouter } from 'react-router-dom'
import './index.css'

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
)
EOL

cat > src/App.tsx <<EOL
import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Home from './pages/Home'

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
    </Routes>
  )
}

export default App
EOL

cat > src/pages/Home.tsx <<EOL
import React from 'react'

const Home: React.FC = () => {
  return <h1>Welcome to EatsAdvisor</h1>
}

export default Home
EOL

cat > src/hooks/useAuth.ts <<EOL
import { useState } from 'react'

export function useAuth() {
  const [user, setUser] = useState(null)

  return { user, setUser }
}
EOL

cat > src/services/api.ts <<EOL
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:5000',
  headers: { 'Content-Type': 'application/json' }
})

export default api
EOL

cat > src/types/user.ts <<EOL
export interface User {
  id: string
  name: string
  email: string
}
EOL

cat > .env.example <<EOL
VITE_API_URL=http://localhost:5000
EOL

# Step 5: Initialize Git repository
echo "Initializing Git repository..."
git init
git add .
git commit -m "Initial commit - Vite + React + TypeScript setup"

# Step 6: Success message
echo "✅ Client setup complete! Run the following to start development:"
echo "cd $PROJECT_NAME && npm run dev"
