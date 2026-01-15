import React, { useState, useEffect } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api/products";

function App() {
  const [user, setUser] = useState(null); // Состояние авторизации
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [products, setProducts] = useState([]);
  const [newProduct, setNewProduct] = useState({ name: "", quantity: 0, price: 0.0 });
  const [error, setError] = useState("");

  const getAuthHeader = () => {
    return "Basic " + btoa(`${user.username}:${user.password}`);
  };

  const handleLogin = (e) => {
    e.preventDefault();
    // Простая проверка "на лету" (реальная проверка произойдет при первом fetch)
    setUser({ username, password });
  };

  const fetchProducts = () => {
    if (!user) return;
    
    fetch(API_URL, {
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
        if(err.message === "Неверный логин или пароль") setUser(null);
      });
  };

  useEffect(() => {
    if (user) fetchProducts();
  }, [user]);

  const handleAdd = (e) => {
    e.preventDefault();
    fetch(API_URL, {
      method: "POST",
      headers: { 
        "Content-Type": "application/json", 
        Authorization: getAuthHeader() 
      },
      body: JSON.stringify(newProduct),
    }).then(() => {
      fetchProducts();
      setNewProduct({ name: "", quantity: 0, price: 0.0 });
    });
  };

  const handleDelete = (id) => {
    fetch(`${API_URL}/${id}`, {
      method: "DELETE",
      headers: { Authorization: getAuthHeader() },
    }).then(() => fetchProducts());
  };

  const downloadPdf = () => {
    fetch(`${API_URL}/invoice`, {
      headers: { Authorization: getAuthHeader() },
    })
      .then((res) => res.blob())
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "Lieferschein_Altenburg.pdf";
        a.click();
      });
  };

  // Если не авторизован - показываем форму входа
  if (!user) {
    return (
      <div className="login-container" style={{ padding: "20px", maxWidth: "400px", margin: "0 auto" }}>
        <h2>System Login (Mini ERP)</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleLogin} style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <input 
            type="text" 
            placeholder="Username (admin)" 
            value={username} 
            onChange={e => setUsername(e.target.value)} 
          />
          <input 
            type="password" 
            placeholder="Password (admin123)" 
            value={password} 
            onChange={e => setPassword(e.target.value)} 
          />
          <button type="submit">Войти</button>
        </form>
      </div>
    );
  }

  // Основной интерфейс
  return (
    <div className="App" style={{ padding: "20px" }}>
      <header style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h1>Mini ERP: Warenbestand</h1>
        <button onClick={() => setUser(null)} style={{ background: "#f44336" }}>Выйти</button>
      </header>

      <div className="controls" style={{ margin: "20px 0", padding: "15px", background: "#f5f5f5" }}>
        <h3>Neues Produkt</h3>
        <input
          placeholder="Produktname"
          value={newProduct.name}
          onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
        />
        <input
          type="number"
          placeholder="Menge"
          value={newProduct.quantity}
          onChange={(e) => setNewProduct({ ...newProduct, quantity: parseInt(e.target.value) })}
        />
        <input
          type="number"
          placeholder="Preis"
          value={newProduct.price}
          onChange={(e) => setNewProduct({ ...newProduct, price: parseFloat(e.target.value) })}
        />
        <button onClick={handleAdd}>Hinzufügen</button>
      </div>

      <button onClick={downloadPdf} style={{ marginBottom: "20px", background: "#4CAF50", color: "white" }}>
        📄 PDF Lieferschein herunterladen
      </button>

      <table border="1" cellPadding="10" style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Produktname</th>
            <th>Menge (Stück)</th>
            <th>Preis (€)</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.quantity}</td>
              <td>{p.price.toFixed(2)} €</td>
              <td>
                <button onClick={() => handleDelete(p.id)} style={{ background: "#ff9800" }}>Löschen</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;
