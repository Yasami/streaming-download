import React, { useState } from 'react';
import axios from 'axios'
import fileDownload from 'js-file-download'
import './App.css';

const fromOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
const toOptions = [10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]

function App() {
  const [from, setFrom] = useState(10);
  const [to, setTo] = useState(12);
  const [downloadingAxios, setDownloadingAxios] = useState(false)
  const [downloadedSizeAxios, setDownloadedSizeAxios] = useState(0)
  const [downloadingFetch, setDownloadingFetch] = useState(false)
  const [downloadedSizeFetch, setDownloadedSizeFetch] = useState(0)

  const downloadAxios = async () => {
      const response = await axios.get('http://localhost:9000/download', {
          headers: {
              apikey: "abcd",
          },
          responseType: 'stream',
          params: {from: from, to: to},
          onDownloadProgress: progressEvent => {
              console.log(progressEvent)
              setDownloadedSizeAxios(progressEvent.loaded)
              setDownloadingAxios(true)
          }
      })
      console.log(response.statusText)
      setDownloadingAxios(false)
      fileDownload(response.data, `random_${from}_${to}.csv`)
  }

  const downloadFetch = async () => {
      const response = await fetch(`http://localhost:9000/download?from=${from}&to=${to}`, {
          headers: {
              apikey: "abcd",
          },
      })
      setDownloadingFetch(true)
      console.log(response.statusText)
      const b = await response.blob()
      setDownloadingFetch(false)
      fileDownload(b, `random_${from}_${to}.csv`)
  }

  return (
    <div className="App">
      <header className="App-header">
          <label>From</label>
          <select value={from} onChange={(e) => { setFrom(Number(e.target.value)) }}>
              {fromOptions.map((n) => <option key={n} value={n}>{n}</option>)}
          </select>
          <label>To</label>
          <select value={to} onChange={(e) => { setTo(Number(e.target.value)) }}>
              {toOptions.map((n) => <option key={n} value={n}>{n}</option>)}
          </select>
          <div>
              <button onClick={downloadAxios} disabled={downloadingAxios}>Download using Axios</button>
              <p>{downloadedSizeAxios} bytes downloaded</p>
          </div>
          <div>
              <button onClick={downloadFetch} disabled={downloadingFetch}>Download using Fetch</button>
              {/*<p>{downloadedSizeFetch} bytes downloaded</p>*/}
          </div>
      </header>
    </div>
  );
}

export default App;
