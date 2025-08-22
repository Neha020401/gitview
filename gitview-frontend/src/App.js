import React, { useState, useEffect } from "react"
import axios from "axios"


function App() {
  const [repoUrl, setRepoUrl] = useState("")
  const [branchName, setBranchName] = useState("")
  const [previews, setPreviews] = useState([])
  const [isLoading, setIsLoading] = useState(false)

  // Fetch previews on load
  useEffect(() => {
    fetchPreviews()
  }, [])

  const fetchPreviews = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/previews")
      setPreviews(res.data)
    } catch (err) {
      console.error("Error fetching previews", err)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)
    try {
      await axios.post("http://localhost:8080/api/repos", null, {
        params: { repoUrl, branchName },
      })
      fetchPreviews() // refresh list
      setRepoUrl("")
      setBranchName("")
    } catch (err) {
      alert("Failed to add repo: " + err.message)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        {/* Header */}
        <div className="text-center mb-12">
          <div className="inline-flex items-center gap-3 mb-4">
            <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center">
              <span className="text-2xl">🚀</span>
            </div>
            <h1 className="text-4xl font-bold text-slate-800">Git Branch Previewer</h1>
          </div>
          <p className="text-slate-600 text-lg">Deploy and preview your Git branches instantly</p>
        </div>

        {/* Add Repository Form */}
        <div className="bg-white rounded-2xl shadow-lg border border-slate-200 p-8 mb-8">
          <h2 className="text-2xl font-semibold text-slate-800 mb-6 flex items-center gap-2">
            <span className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
              <span className="text-green-600 text-sm">+</span>
            </span>
            Add New Repository
          </h2>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Repository URL</label>
                <input
                  type="text"
                  value={repoUrl}
                  onChange={(e) => setRepoUrl(e.target.value)}
                  className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  placeholder="https://github.com/username/repo.git"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Branch Name</label>
                <input
                  type="text"
                  value={branchName}
                  onChange={(e) => setBranchName(e.target.value)}
                  className="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  placeholder="main, develop, feature/new-ui"
                  required
                />
              </div>
            </div>

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={isLoading}
                className="px-8 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                {isLoading ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    Adding...
                  </>
                ) : (
                  <>
                    <span>+</span>
                    Add Repository
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Previews Section */}
        <div className="bg-white rounded-2xl shadow-lg border border-slate-200 p-8">
          <h2 className="text-2xl font-semibold text-slate-800 mb-6 flex items-center gap-2">
            <span className="w-6 h-6 bg-purple-100 rounded-full flex items-center justify-center">
              <span className="text-purple-600 text-sm">👁</span>
            </span>
            Available Previews
            <span className="ml-auto text-sm font-normal text-slate-500 bg-slate-100 px-3 py-1 rounded-full">
              {previews.length} {previews.length === 1 ? "preview" : "previews"}
            </span>
          </h2>

          {previews.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl text-slate-400">📋</span>
              </div>
              <h3 className="text-lg font-medium text-slate-600 mb-2">No previews yet</h3>
              <p className="text-slate-500">Add your first repository to get started</p>
            </div>
          ) : (
            <div className="grid gap-4">
              {previews.map((preview, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between p-4 border border-slate-200 rounded-lg hover:border-slate-300 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                      <span className="text-white font-medium text-sm">
                        {preview.branchName.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div>
                      <h3 className="font-medium text-slate-800">{preview.branchName}</h3>
                      <p className="text-sm text-slate-500">Branch preview</p>
                    </div>
                  </div>

                  <a
                    href={preview.url}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-2 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg transition-colors"
                  >
                    <span>View Preview</span>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                      />
                    </svg>
                  </a>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default App
