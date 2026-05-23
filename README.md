# Collaborative Whiteboard Project

## Prerequisites
- **Node.js**: You must have Node.js installed. You can download it from [nodejs.org](https://nodejs.org/).
- **Visual Studio Code**: Recommended editor.

## Recommended VS Code Extensions
When you open this project in VS Code, it should recommend the following extensions:
1.  **ESLint** (`dbaeumer.vscode-eslint`): For code linting.
2.  **Prettier - Code formatter** (`esbenp.prettier-vscode`): For code formatting.
3.  **ES7+ React/Redux/React-Native snippets** (`dsznajder.es7-react-js-snippets`): For React code snippets.

## How to Run in VS Code

### 1. Open the Project
Double-click the `CollaborativeWhiteboard.code-workspace` file to open the project in VS Code with the correct configuration.

### 2. Start the Backend
1.  Open a terminal in VS Code (`Ctrl + ~`).
2.  Navigate to the backend folder (if not already there) or open a new terminal for the backend.
    ```bash
    cd backend
    ```
3.  Install dependencies (if you haven't already):
    ```bash
    npm install
    ```
4.  Start the server:
    ```bash
    node server.js
    ```
    You should see: `Server running on port 3001`

### 3. Start the Frontend
1.  Open a **second** terminal in VS Code (click the `+` icon in the terminal panel).
2.  Navigate to the frontend folder:
    ```bash
    cd frontend
    ```
3.  Install dependencies (if you haven't already):
    ```bash
    npm install
    ```
4.  Start the development server:
    ```bash
    npm run dev
    ```
    You should see a URL like `http://localhost:5173`.

### 4. Verify
Open the frontend URL (e.g., `http://localhost:5173`) in two different browser tabs. Draw in one and watch it appear in the other!
