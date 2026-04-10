// Kompletní refaktorovaný script.js

window.temperatureChart = null;

function parseJsonAttribute(el, attrName) {
  const raw = el.getAttribute(attrName);
  if (!raw) return [];
  try {
    return JSON.parse(raw);
  } catch (e) {
    console.error(`Chyba při parsování atributu ${attrName}:`, e);
    return [];
  }
}

function renderTemperatureChart(canvas) {
  const labels = parseJsonAttribute(canvas, "data-labels");
  const data = parseJsonAttribute(canvas, "data-datapoints");

  if (!Array.isArray(labels) || !Array.isArray(data) || labels.length === 0 || data.length === 0) {
    console.warn("Nejsou k dispozici žádná data pro graf.");
    return;
  }

  const ctx = canvas.getContext("2d");
  window.temperatureChart = new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [
        {
          label: "Teplota v čase",
          data: data,
          borderWidth: 2,
          tension: 0.3,
          fill: false,
          borderColor: "#007bff"
        },
      ],
    },
    options: {
      responsive: true,
      scales: {
        y: {
          title: {
            display: true,
            text: "Teplota [°C]"
          },
        },
        x: {
          ticks: {
            maxRotation: 90,
            minRotation: 45,
            autoSkip: true,
          },
        },
      },
      plugins: {
        legend: {
          display: true,
        },
        tooltip: {
          mode: "index",
          intersect: false,
        },
      },
    },
  });
}

function updateTable(data) {
  const table = document.getElementById("dataTable");
  if (!table) return;

  const tbody = table.querySelector("tbody");
  tbody.innerHTML = "";
  data.forEach(item => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${item.id}</td>
      <td>${item.temperature}</td>
      <td>${item.timestamp_measurement}</td>
      <td>${item.timestamp_send}</td>
      <td>${item.timestamp_received}</td>
    `;
    tbody.appendChild(row);
  });
}

function updateLastValue(last) {
  const container = document.getElementById("lastValue");
  if (!container || !last) return;
  container.innerHTML = `
    <h5>Poslední naměřená hodnota: ${last.temperature} °C</h5>
    <p>Čas zápisu: ${last.timestamp_measurement}</p>
  `;
}

function handleDeleteOldest(e) {
  e.preventDefault();
  const form = e.target.closest("form");
  const count = form.querySelector("input[name='count']").value;

  fetch(form.action, {
    method: "POST",
    headers: { "X-Requested-With": "XMLHttpRequest" },
    body: new URLSearchParams({ count })
  })
    .then((res) => res.json())
    .then((data) => {
      if (data.success) {
        updateTable(data.displayed_data);
        updateLastValue(data.last_value);
      } else {
        alert("Chyba: " + data.message);
      }
    })
    .catch((err) => {
      console.error("Chyba při mazání:", err);
    });
}

document.addEventListener("DOMContentLoaded", () => {
  const canvas = document.getElementById("temperatureChart");
  if (canvas) {
    renderTemperatureChart(canvas);
  }

  const deleteForm = document.getElementById("deleteForm");
  if (deleteForm) {
    deleteForm.addEventListener("submit", handleDeleteOldest);
  }
});
