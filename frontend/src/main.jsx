import React, { createContext, useContext, useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  BrowserRouter,
  Link,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useParams,
} from "react-router-dom";
import { api } from "./api";
import "./styles.css";
const AuthContext = createContext(null);
const getSession = () => {
  try {
    return JSON.parse(localStorage.getItem("session"));
  } catch {
    return null;
  }
};
function AuthProvider({ children }) {
  const [session, setSession] = useState(null);
  const [checkingSession, setCheckingSession] = useState(true);

  useEffect(() => {
    const storedSession = getSession();
    const token = localStorage.getItem("token");
    if (!storedSession || !token) {
      localStorage.removeItem("token");
      localStorage.removeItem("session");
      setCheckingSession(false);
      return;
    }
    api("/auth/me")
      .then((profile) => {
        const verifiedSession = { ...storedSession, ...profile };
        localStorage.setItem("session", JSON.stringify(verifiedSession));
        setSession(verifiedSession);
      })
      .catch(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("session");
      })
      .finally(() => setCheckingSession(false));
  }, []);

  function login(authResponse) {
    localStorage.setItem("token", authResponse.token);
    localStorage.setItem("session", JSON.stringify(authResponse));
    setSession(authResponse);
  }
  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("session");
    setSession(null);
  }
  return <AuthContext.Provider value={{ session, checkingSession, login, logout }}>{children}</AuthContext.Provider>;
}
function useAuth() { return useContext(AuthContext); }
function Shell({ children }) {
  const { session, logout } = useAuth();
  const nav = useNavigate();
  function handleLogout() {
    logout();
    nav("/login");
  }
  return (
    <>
      <header>
        <Link className="brand" to={session?.role === "ADMIN" ? "/admin" : "/"}>
          Exam<span>Flow</span>
        </Link>
        {session && (
          <nav>
            <span>Hi, {session.name}</span>
            <Link to={session.role === "ADMIN" ? "/admin" : "/"}>
              Dashboard
            </Link>
            {session.role === "USER" && <Link to="/results">My results</Link>}
            {session.role === "ADMIN" && (
              <Link to="/admin/results">Results</Link>
            )}
            <button className="link" onClick={handleLogout}>
              Log out
            </button>
          </nav>
        )}
      </header>
      <main>{children}</main>
    </>
  );
}
function Login() {
  const nav = useNavigate();
  const { login } = useAuth();
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const data = await api(
        "/auth/" + (mode === "login" ? "login" : "signup"),
        { method: "POST", body: JSON.stringify(form) },
      );
      login(data);
      nav(data.role === "ADMIN" ? "/admin" : "/");
    } catch (e) {
      setError(e.message);
    }
  }
  return (
    <div className="auth card">
      <h1>{mode === "login" ? "Welcome back" : "Create your account"}</h1>
      <p>Sign in to continue your examination journey.</p>
      <form onSubmit={submit}>
        {mode === "signup" && (
          <label>
            Name
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </label>
        )}
        <label>
          Email
          <input
            required
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </label>
        <label>
          Password
          <input
            required
            minLength={mode === "signup" ? 6 : undefined}
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
        </label>
        {error && <div className="error">{error}</div>}
        <button> {mode === "login" ? "Sign in" : "Sign up"} </button>
      </form>
      <button
        className="link"
        onClick={() => setMode(mode === "login" ? "signup" : "login")}
      >
        {mode === "login"
          ? "Need an account? Sign up"
          : "Already registered? Sign in"}
      </button>
      <small>Admin: admin@gmail.com / admin</small>
    </div>
  );
}
function Protected({ admin = false, children }) {
  const { session, checkingSession } = useAuth();
  if (checkingSession) return <p>Checking your secure session…</p>;
  return !session ? (
    <Navigate to="/login" />
  ) : admin && session.role !== "ADMIN" ? (
    <Navigate to="/" />
  ) : (
    children
  );
}
function UserDashboard() {
  const [tests, setTests] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => {
    api("/tests")
      .then(setTests)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <section>
      <div className="hero">
        <h1>Available examinations</h1>
        <p>
          Choose a test when you’re ready. Your timer begins once you start.
        </p>
      </div>
      {error && <div className="error">{error}</div>}
      <div className="grid">
        {tests.map((t) => (
          <article className="card" key={t.id}>
            <h2>{t.title}</h2>
            <p>{t.description || "No description provided."}</p>
            <div className="meta">
              {t.questionCount} questions · {t.durationMinutes} minutes
            </div>
            <Link className="button" to={"/test/" + t.id}>
              Start test
            </Link>
          </article>
        ))}
        {!tests.length && !error && <p>No examinations are available yet.</p>}
      </div>
    </section>
  );
}
function Exam() {
  const { id } = useParams();
  const nav = useNavigate();
  const [data, setData] = useState(null);
  const [answers, setAnswers] = useState({});
  const [remaining, setRemaining] = useState(0);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    api("/tests/" + id + "/start", { method: "POST" })
      .then((d) => {
        setData(d);
        setRemaining(
          Math.max(0, Math.floor((new Date(d.expiresAt) - Date.now()) / 1000)),
        );
      })
      .catch((e) => setError(e.message));
  }, [id]);
  useEffect(() => {
    if (!data) return;
    if (remaining <= 0) {
      submit();
      return;
    }
    const x = setInterval(() => setRemaining((r) => r - 1), 1000);
    return () => clearInterval(x);
  }, [remaining, data]);
  async function submit() {
    if (busy || !data) return;
    setBusy(true);
    try {
      const r = await api("/tests/" + id + "/submit", {
        method: "POST",
        body: JSON.stringify({
          answers: Object.entries(answers).map(
            ([questionId, selectedOption]) => ({
              questionId: Number(questionId),
              selectedOption,
            }),
          ),
        }),
      });
      nav("/result/" + r.attemptId, { state: r });
    } catch (e) {
      setError(e.message);
      setBusy(false);
    }
  }
  if (error) return <div className="error">{error}</div>;
  if (!data) return <p>Preparing your examination…</p>;
  return (
    <section className="exam">
      <div className="exam-head">
        <div>
          <h1>{data.test.title}</h1>
          <p>{data.test.description}</p>
        </div>
        <strong className={remaining < 60 ? "danger" : ""}>
          {String(Math.floor(remaining / 60)).padStart(2, "0")}:
          {String(remaining % 60).padStart(2, "0")}
        </strong>
      </div>
      {data.test.questions.map((q, i) => (
        <article className="question card" key={q.id}>
          <h3>
            {i + 1}. {q.text}
          </h3>
          {["A", "B", "C", "D"].map((k) => (
            <label className="option" key={k}>
              <input
                type="radio"
                name={"q" + q.id}
                checked={answers[q.id] === k}
                onChange={() => setAnswers({ ...answers, [q.id]: k })}
              />
              {k}. {q["option" + k]}
            </label>
          ))}
        </article>
      ))}
      <button disabled={busy} onClick={submit}>
        {busy ? "Submitting…" : "Submit examination"}
      </button>
    </section>
  );
}
function Results({ admin = false }) {
  const [rows, setRows] = useState([]);
  useEffect(() => {
    api(admin ? "/admin/results" : "/results/me").then(setRows);
  }, [admin]);
  return (
    <section>
      <h1>{admin ? "All results" : "My results"}</h1>
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              {admin && <th>Candidate</th>}
              <th>Test</th>
              <th>Correct</th>
              <th>Score</th>
              <th>Submitted</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.attemptId}>
                {admin && <td>{r.userName}</td>}
                <td>{r.testTitle}</td>
                <td>
                  {r.correctAnswers}/{r.totalQuestions}
                </td>
                <td>
                  <b>{r.percentage}%</b>
                </td>
                <td>
                  {r.submittedAt
                    ? new Date(r.submittedAt).toLocaleString()
                    : "In progress"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!rows.length && <p>No results yet.</p>}
      </div>
    </section>
  );
}
function Result() {
  const nav = useNavigate();
  const r = history.state?.usr;
  if (!r) return <Navigate to="/results" />;
  return (
    <div className="result card">
      <div className="score">{r.percentage}%</div>
      <h1>Test submitted</h1>
      <p>
        You answered <b>{r.correctAnswers}</b> out of <b>{r.totalQuestions}</b>{" "}
        questions correctly.
      </p>
      <button onClick={() => nav("/")}>Back to dashboard</button>
    </div>
  );
}
function Admin() {
  const [tests, setTests] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    title: "",
    description: "",
    durationMinutes: 30,
    published: true,
  });
  const [selected, setSelected] = useState(null);
  const [q, setQ] = useState({
    text: "",
    optionA: "",
    optionB: "",
    optionC: "",
    optionD: "",
    correctOption: "A",
  });
  async function load() {
    try {
      const testSummaries = await api("/admin/tests");
      setTests(testSummaries);
      return testSummaries;
    } catch (requestError) {
      setError(requestError.message);
      return [];
    }
  }
  useEffect(() => {
    load();
  }, []);
  async function create(e) {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      await api("/admin/tests", {
        method: "POST",
        body: JSON.stringify({ ...form, durationMinutes: Number(form.durationMinutes) }),
      });
      setForm({ title: "", description: "", durationMinutes: 30, published: true });
      setMessage("Test created successfully. Select it below to add questions.");
      await load();
    } catch (requestError) {
      setError(requestError.message);
    }
  }
  async function addQuestion(e) {
    e.preventDefault();
    if (!selected) return;
    setError("");
    setMessage("");
    try {
      await api("/admin/tests/" + selected.id + "/questions", {
        method: "POST",
        body: JSON.stringify(q),
      });
      setQ({ text: "", optionA: "", optionB: "", optionC: "", optionD: "", correctOption: "A" });
      setMessage("Question added successfully.");
      await load();
      setSelected(await api("/admin/tests/" + selected.id));
    } catch (requestError) {
      setError(requestError.message);
    }
  }
  async function selectTest(test) {
    setError("");
    setMessage("");
    try {
      setSelected(await api("/admin/tests/" + test.id));
    } catch (requestError) {
      setError(requestError.message);
    }
  }
  return (
    <section>
      <h1>Administration</h1>
      {error && <div className="error">Could not save: {error}</div>}
      {message && <div className="success">{message}</div>}
      <div className="admin-layout">
        <form className="card" onSubmit={create}>
          <h2>Create test</h2>
          <label>
            Title
            <input
              required
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
          </label>
          <label>
            Description
            <textarea
              value={form.description}
              onChange={(e) =>
                setForm({ ...form, description: e.target.value })
              }
            />
          </label>
          <label>
            Duration (minutes)
            <input
              type="number"
              min="1"
              value={form.durationMinutes}
              onChange={(e) =>
                setForm({ ...form, durationMinutes: e.target.value })
              }
            />
          </label>
          <label className="option">
            <input
              type="checkbox"
              checked={form.published}
              onChange={(e) =>
                setForm({ ...form, published: e.target.checked })
              }
            />
            Publish immediately
          </label>
          <button>Create test</button>
        </form>
        <div>
          <h2>Your tests</h2>
          {tests.map((t) => (
            <article className="card compact" key={t.id}>
              <h3>{t.title}</h3>
              <p>
                {t.questionCount} questions · {t.durationMinutes} min
              </p>
              <button type="button" onClick={() => selectTest(t)}>Manage questions</button>
            </article>
          ))}
        </div>
      </div>
      {selected && (
        <form className="card question-form" onSubmit={addQuestion}>
          <h2>Add question to: {selected.title}</h2>
          <p>{selected.questions?.length || 0} question(s) currently added.</p>
          <label>
            Question
            <input
              required
              value={q.text}
              onChange={(e) => setQ({ ...q, text: e.target.value })}
            />
          </label>
          {["A", "B", "C", "D"].map((k) => (
            <label key={k}>
              Option {k}
              <input
                required
                value={q["option" + k]}
                onChange={(e) => setQ({ ...q, ["option" + k]: e.target.value })}
              />
            </label>
          ))}
          <label>
            Correct option
            <select
              value={q.correctOption}
              onChange={(e) => setQ({ ...q, correctOption: e.target.value })}
            >
              {["A", "B", "C", "D"].map((x) => (
                <option key={x}>{x}</option>
              ))}
            </select>
          </label>
          <button type="submit">Add question</button>
          {selected.questions?.length > 0 && (
            <div className="question-list">
              <h3>Added questions</h3>
              {selected.questions.map((question, index) => (
                <div className="question-preview" key={question.id}>
                  <b>{index + 1}. {question.text}</b>
                  <span>A. {question.optionA} · B. {question.optionB} · C. {question.optionC} · D. {question.optionD}</span>
                </div>
              ))}
            </div>
          )}
        </form>
      )}
    </section>
  );
}
function App() {
  return (
    <Shell>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <Protected>
              <UserDashboard />
            </Protected>
          }
        />
        <Route
          path="/test/:id"
          element={
            <Protected>
              <Exam />
            </Protected>
          }
        />
        <Route
          path="/result/:id"
          element={
            <Protected>
              <Result />
            </Protected>
          }
        />
        <Route
          path="/results"
          element={
            <Protected>
              <Results />
            </Protected>
          }
        />
        <Route
          path="/admin"
          element={
            <Protected admin>
              <Admin />
            </Protected>
          }
        />
        <Route
          path="/admin/results"
          element={
            <Protected admin>
              <Results admin />
            </Protected>
          }
        />
      </Routes>
    </Shell>
  );
}
createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <AuthProvider><App /></AuthProvider>
  </BrowserRouter>,
);
