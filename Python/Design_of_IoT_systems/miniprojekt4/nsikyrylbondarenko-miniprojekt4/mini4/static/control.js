// Odeslání příkazu na server
function sendCommand(command) {
  fetch("/send_command", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(command)
  })
    .then((res) => res.json())
    .then((data) => handleServerResponse(data, command))
    .catch((err) => handleError(err));
}

// Zpracování odpovědi ze serveru
function handleServerResponse(data, command) {
  if (!data.success) {
    showAlert("danger", "Chyba: " + data.message);
    return;
  }

  if (command.led) {
    updateStatus("led", command.led);
    localStorage.setItem("ledStatus", command.led);
  }

  if (command.measure) {
    updateStatus("measure", command.measure);
    localStorage.setItem("measureStatus", command.measure);
  }

  showAlert("success", data.message || "Příkaz odeslán.");
}

// Zobrazení chybové zprávy
function handleError(err) {
  console.error("Chyba při odesílání příkazu:", err);
  showAlert("danger", "Nepodařilo se odeslat příkaz.");
}

// Aktualizace stavu na stránce
function updateStatus(type, value) {
  const el = document.getElementById(`${type}Status`);
  if (el) {
    el.textContent = value === "on" || value === "start" ? "Aktivní" : "Neaktivní";
    el.className = "fw-bold " + (value === "on" || value === "start" ? "text-success" : "text-secondary");
  }
}

// Zobrazení zprávy uživateli
function showAlert(level, message) {
  const flashDiv = document.getElementById("flashMessage");
  if (flashDiv) {
    flashDiv.innerHTML = `<div class="alert alert-${level}">${message}</div>`;
  }
}

// Obnovení stavu po načtení stránky
document.addEventListener("DOMContentLoaded", () => {
  updateStatus("led", localStorage.getItem("ledStatus") || "off");
  updateStatus("measure", localStorage.getItem("measureStatus") || "stop");
});
