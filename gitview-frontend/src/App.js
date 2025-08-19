import React, { useState, useEffect } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

function App() {
  const [repoUrl, setRepoUrl] = useState("");
  const [branchName, setBranchName] = useState("");
  const [previews, setPreviews] = useState([]);

  // Fetch previews on load
  useEffect(() => {
    fetchPreviews();
  }, []);

  const fetchPreviews = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/previews");
      setPreviews(res.data);
    } catch (err) {
      console.error("Error fetching previews", err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8080/api/repos", null, {
        params: { repoUrl, branchName }
      });
      fetchPreviews(); // refresh list
      setRepoUrl("");
      setBranchName("");
    } catch (err) {
      alert("Failed to add repo: " + err.message);
    }
  };

  return (
    <div className="container mt-4">
      <h2>🚀 Git Branch Previewer</h2>

      {/* Repo Form */}
      <form onSubmit={handleSubmit} className="mb-4">
        <div className="row">
          <div className="col-md-6">
            <input
              type="text"
              value={repoUrl}
              onChange={(e) => setRepoUrl(e.target.value)}
              className="form-control"
              placeholder="Enter Git Repo URL"
              required
            />
          </div>
          <div className="col-md-4">
            <input
              type="text"
              value={branchName}
              onChange={(e) => setBranchName(e.target.value)}
              className="form-control"
              placeholder="Enter Branch Name"
              required
            />
          </div>
          <div className="col-md-2">
            <button type="submit" className="btn btn-primary w-100">
              Add
            </button>
          </div>
        </div>
      </form>

      {/* Previews Table */}
      <h4>Available Previews</h4>
      <table className="table table-bordered">
        <thead>
          <tr>
            <th>Branch</th>
            <th>Preview URL</th>
          </tr>
        </thead>
        <tbody>
          {previews.map((preview, index) => (
            <tr key={index}>
              <td>{preview.branchName}</td>
              <td>
                <a href={preview.url} target="_blank" rel="noreferrer">
                  {preview.url}
                </a>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;
