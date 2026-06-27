import React, { useState, useEffect, useRef } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api/v1/products";

function App() {
  const [user, setUser] = useState(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [products, setProducts] = useState([]);
  const [newProduct, setNewProduct] = useState({ name: "", quantity: 0, price: 0.0 });
  const [editingProduct, setEditingProduct] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const isFirstFetch = useRef(true);

  const getAuthHeader = () => "Basic " + btoa(`${user.username}:${user.password}`);

  const handleLogin = (e) => {
    e.preventDefault();
    setUser({ username, password });
  };

  const fetchProducts = () => {
    if (!user) return;
    
    const url = searchTerm ? `${API_URL}/search?name=${encodeURIComponent(searchTerm)}` : API_URL;

    setLoading(true);
    fetch(url, {
      headers: { Authorization: getAuthHeader() },
    })
      .then((res) => {
        if (res.status === 401) throw new Error("Неверный логин или пароль");
        if (!res.ok) throw new Error("Ошибка сервера");
        return res.json();
      })
      .then((data) => {
        setProducts(data);
        setError("");
      })
      .catch((err) => {
        setError(err.message);
        if (err.message === "Неверный логин или пароль") setUser(null);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (!user) {
      isFirstFetch.current = true;
      return;
    }
    // Load instantly right after login, but debounce subsequent search typing:
    // collapse rapid keystrokes into one request so the backend is not hit on
    // every character and the list cannot flicker through stale, out-of-order
    // responses.
    if (isFirstFetch.current) {
      isFirstFetch.current = false;
      fetchProducts();
      return;
    }
    const timer = setTimeout(fetchProducts, 300);
    return () => clearTimeout(timer);
  }, [user, searchTerm]);

  const handleAdd = (e) => {
    e.preventDefault();
    fetch(API_URL, {
      method: "POST",
      headers: { 
        "Content-Type": "application/json", 
        Authorization: getAuthHeader() 
      },
      body: JSON.stringify(newProduct),
    })
      .then(res => {
        if (!res.ok) return res.json().then(err => { throw err; });
        return res.json();
      })
      .then(() => {
        fetchProducts();
        setNewProduct({ name: "", quantity: 0, price: 0.0 });
        setError("");
      })
      .catch(err => {
        setError(err.errors ? Object.values(err.errors).join(", ") : "Ошибка при добавлении");
      });
  };

  const handleUpdate = (e) => {
    e.preventDefault();
    fetch(`${API_URL}/${editingProduct.id}`, {
      method: "PUT",
      headers: { 
        "Content-Type": "application/json", 
        Authorization: getAuthHeader() 
      },
      body: JSON.stringify(editingProduct),
    })
      .then(res => {
        if (!res.ok) return res.json().then(err => { throw err; });
        return res.json();
      })
      .then(() => {
        fetchProducts();
        setEditingProduct(null);
        setError("");
      })
      .catch(err => {
        setError(err.errors ? Object.values(err.errors).join(", ") : "Ошибка при обновлении");
      });
  };

  const handleDelete = (id) => {
    if (!window.confirm("Вы уверены, что хотите удалить этот продукт?")) return;
    
    fetch(`${API_URL}/${id}`, {
      method: "DELETE",
      headers: { Authorization: getAuthHeader() },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Ошибка при удалении");
        fetchProducts();
        setError("");
      })
      .catch(() => setError("Ошибка при удалении"));
  };

  const downloadPdf = () => {
    fetch(`${API_URL}/invoice`, {
      headers: { Authorization: getAuthHeader() },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Ошибка при создании PDF");
        return res.blob();
      })
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "Lieferschein_Altenburg.pdf";
        a.click();
        window.URL.revokeObjectURL(url);
        setError("");
      })
      .catch(() => setError("Ошибка при создании PDF"));
  };

  if (!user) {
    return (
      <div className="login-container" style={{ padding: "20px", maxWidth: "400px", margin: "50px auto" }}>
        <h2>🔐 System Login (Mini ERP)</h2>
        {error && <p role="alert" style={{ color: "red", background: "#ffe6e6", padding: "10px" }}>{error}</p>}
        <form onSubmit={handleLogin} style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
          <input
            type="text"
            placeholder="Username (admin)"
            aria-label="Username"
            autoComplete="username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            style={{ padding: "10px", fontSize: "16px" }}
            required
          />
          <input
            type="password"
            placeholder="Password (admin123)"
            aria-label="Password"
            autoComplete="current-password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            style={{ padding: "10px", fontSize: "16px" }}
            required
          />
          <button type="submit" style={{ padding: "10px", fontSize: "16px", background: "#4CAF50", color: "white" }}>
            Войти
          </button>
        </form>
      </div>
    );
  }

  return (
    <div className="App" style={{ padding: "20px", maxWidth: "1200px", margin: "0 auto" }}>
      <header style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
        <h1>📦 Mini ERP: Warenbestand</h1>
        <button onClick={() => setUser(null)} style={{ background: "#f44336", color: "white", padding: "10px 20px" }}>
          Выйти
        </button>
      </header>

      {error && <div role="alert" style={{ color: "red", background: "#ffe6e6", padding: "10px", marginBottom: "20px" }}>{error}</div>}

      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          placeholder="🔍 Поиск продуктов..."
          aria-label="Поиск продуктов"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ padding: "10px", width: "100%", fontSize: "16px" }}
        />
      </div>

      <div className="controls" style={{ margin: "20px 0", padding: "20px", background: "#f5f5f5", borderRadius: "5px" }}>
        <h3>{editingProduct ? "✏️ Редактировать продукт" : "➕ Neues Produkt"}</h3>
        <form onSubmit={editingProduct ? handleUpdate : handleAdd} style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
          <input
            placeholder="Produktname"
            aria-label="Produktname"
            value={editingProduct ? editingProduct.name : newProduct.name}
            onChange={(e) => editingProduct 
              ? setEditingProduct({ ...editingProduct, name: e.target.value })
              : setNewProduct({ ...newProduct, name: e.target.value })}
            style={{ flex: "2", padding: "10px" }}
            required
          />
          <input
            type="number"
            placeholder="Menge"
            aria-label="Menge"
            value={editingProduct ? editingProduct.quantity : newProduct.quantity}
            onChange={(e) => editingProduct 
              ? setEditingProduct({ ...editingProduct, quantity: parseInt(e.target.value) || 0 })
              : setNewProduct({ ...newProduct, quantity: parseInt(e.target.value) || 0 })}
            style={{ flex: "1", padding: "10px" }}
            min="0"
            required
          />
          <input
            type="number"
            step="0.01"
            placeholder="Preis"
            aria-label="Preis"
            value={editingProduct ? editingProduct.price : newProduct.price}
            onChange={(e) => editingProduct 
              ? setEditingProduct({ ...editingProduct, price: parseFloat(e.target.value) || 0 })
              : setNewProduct({ ...newProduct, price: parseFloat(e.target.value) || 0 })}
            style={{ flex: "1", padding: "10px" }}
            min="0.01"
            required
          />
          <button type="submit" style={{ padding: "10px 20px", background: "#4CAF50", color: "white" }}>
            {editingProduct ? "Обновить" : "Hinzufügen"}
          </button>
          {editingProduct && (
            <button type="button" onClick={() => setEditingProduct(null)} style={{ padding: "10px 20px", background: "#9E9E9E", color: "white" }}>
              Отмена
            </button>
          )}
        </form>
      </div>

      <button onClick={downloadPdf} style={{ marginBottom: "20px", background: "#2196F3", color: "white", padding: "10px 20px" }}>
        📄 PDF Lieferschein herunterladen
      </button>

      <div role="region" aria-label="Warenbestand-Tabelle" tabIndex={0} style={{ overflowX: "auto", WebkitOverflowScrolling: "touch" }}>
      <table border="1" cellPadding="12" style={{ width: "100%", borderCollapse: "collapse" }}>
        <caption style={{ position: "absolute", width: "1px", height: "1px", padding: 0, margin: "-1px", overflow: "hidden", clip: "rect(0, 0, 0, 0)", whiteSpace: "nowrap", border: 0 }}>
          Warenbestand: Produkte mit Menge, Preis und Gesamtwert
        </caption>
        <thead style={{ background: "#333", color: "white" }}>
          <tr>
            <th>ID</th>
            <th>Produktname</th>
            <th>Menge (Stück)</th>
            <th>Preis (€)</th>
            <th>Gesamtwert (€)</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="6" role="status" aria-live="polite" style={{ textAlign: "center", padding: "20px", color: "#6e6e6e" }}>
                <span aria-hidden="true">⏳ </span>Lade Produkte...
              </td>
            </tr>
          ) : products.length === 0 ? (
            <tr>
              <td colSpan="6" role="status" aria-live="polite" style={{ textAlign: "center", padding: "20px", color: "#6e6e6e" }}>
                Keine Produkte gefunden
              </td>
            </tr>
          ) : (
            products.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.name}</td>
                <td>{p.quantity}</td>
                <td>{p.price.toFixed(2)} €</td>
                <td style={{ fontWeight: "bold" }}>{(p.price * p.quantity).toFixed(2)} €</td>
                <td>
                  <button
                    onClick={() => setEditingProduct(p)}
                    aria-label={`Изменить: ${p.name}`}
                    style={{ background: "#2196F3", color: "white", marginRight: "5px", padding: "5px 15px" }}
                  >
                    <span aria-hidden="true">✏️ </span>Изменить
                  </button>
                  <button
                    onClick={() => handleDelete(p.id)}
                    aria-label={`Löschen: ${p.name}`}
                    style={{ background: "#ff9800", color: "white", padding: "5px 15px" }}
                  >
                    <span aria-hidden="true">🗑️ </span>Löschen
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
      </div>

      {products.length > 0 && (
        <div style={{ marginTop: "20px", textAlign: "right", fontSize: "18px", fontWeight: "bold" }}>
          GESAMTBESTAND WERT: {products.reduce((sum, p) => sum + (p.price * p.quantity), 0).toFixed(2)} €
        </div>
      )}
    </div>
  );
}

export default App;
