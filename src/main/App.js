import React, { useState, useEffect } from "react";
import "./App.css"; // Добавь простую стилизацию сам

function App() {
  const [products, setProducts] = useState([]);
  const [newProduct, setNewProduct] = useState({ name: "", quantity: 0, price: 0.0 });
  
  // Хардкод авторизации для демо (в реальном проекте используй форму логина)
  const authHeader = "Basic " + btoa("admin:admin123"); 

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = () => {
    fetch("http://localhost:8080/api/products", {
      headers: { Authorization: authHeader },
    })
      .then((res) => res.json())
      .then((data) => setProducts(data));
  };

  const handleAdd = () => {
    fetch("http://localhost:8080/api/products", {
      method: "POST",
      headers: { 
          "Content-Type": "application/json",
          Authorization: authHeader 
      },
      body: JSON.stringify(newProduct),
    }).then(() => {
        fetchProducts();
        setNewProduct({ name: "", quantity: 0, price: 0.0 });
    });
  };

  const handleDelete = (id) => {
    fetch(`http://localhost:8080/api/products/${id}`, {
      method: "DELETE",
      headers: { Authorization: authHeader },
    }).then(() => fetchProducts());
  };

  const downloadPdf = () => {
    fetch("http://localhost:8080/api/products/invoice", {
        headers: { Authorization: authHeader },
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

  return (
    <div style={{ padding: "20px", fontFamily: "Arial" }}>
      <h1>Lagerverwaltungssystem (WMS)</h1>
      
      {/* Форма добавления */}
      <div style={{ marginBottom: "20px", border: "1px solid #ccc", padding: "10px" }}>
        <h3>Neues Produkt hinzufügen</h3>
        <input 
            placeholder="Produktname" 
            value={newProduct.name} 
            onChange={e => setNewProduct({...newProduct, name: e.target.value})} 
        />
        <input 
            type="number" placeholder="Menge" 
            value={newProduct.quantity} 
            onChange={e => setNewProduct({...newProduct, quantity: parseInt(e.target.value)})} 
        />
        <input 
            type="number" placeholder="Preis" 
            value={newProduct.price} 
            onChange={e => setNewProduct({...newProduct, price: parseFloat(e.target.value)})} 
        />
        <button onClick={handleAdd} style={{ marginLeft: "10px", backgroundColor: "#4CAF50", color: "white" }}>
            Speichern
        </button>
      </div>

      {/* Кнопка отчета */}
      <button onClick={downloadPdf} style={{ marginBottom: "20px", backgroundColor: "#008CBA", color: "white", padding: "10px" }}>
        📄 PDF Lieferschein herunterladen
      </button>

      {/* Таблица */}
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ backgroundColor: "#f2f2f2", textAlign: "left" }}>
            <th style={{ padding: "10px" }}>ID</th>
            <th>Produktname</th>
            <th>Menge (Stück)</th>
            <th>Preis (€)</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id} style={{ borderBottom: "1px solid #ddd" }}>
              <td style={{ padding: "10px" }}>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.quantity}</td>
              <td>{p.price.toFixed(2)} €</td>
              <td>
                <button onClick={() => handleDelete(p.id)} style={{ color: "red" }}>
                    Löschen
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;
