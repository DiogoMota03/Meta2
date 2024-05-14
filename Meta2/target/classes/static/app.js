var stompClient = null;

// Conjunto de instruções que vai mexer com frontend (acho eu)
function setConnected(connected) {
    if (connected) {
        document.getElementById("connect").setAttribute("disabled", '');
        document.getElementById("disconnect").removeAttribute("disabled");
        document.getElementById("conversation").style.display = 'block';
    }
    else {
        document.getElementById("conversation").style.display = 'hidden';
        document.getElementById("connect").removeAttribute("disabled");
        document.getElementById("disconnect").setAttribute("disabled", '');
    }
    document.getElementById("messages").innerHTML = "";
}

function connect() {
    var socket = new SockJS('/my-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        // Subscribe to the topic
        // The server will send messages to this topic
        stompClient.subscribe('/topic/messages', function (message) {
            showMessage(JSON.parse(message.body).content);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendMessage() {
    // Send a message to the server
    stompClient.send("/app/message", {},
        JSON.stringify({'username': document.getElementById("username").value, 'content': document.getElementById("message").value }));
}

function showMessage(message) {
    // Manipular elementos frontend
    document.getElementById("messages").insertRow();
    document.getElementById("messages").append(message);
}

window.addEventListener('load',
    function () {
        document.getElementById("connect").addEventListener('click', (e) => {
            e.preventDefault();
            connect();
        });
        document.getElementById("disconnect").addEventListener('click', (e) => {
            e.preventDefault();
            disconnect();
        });
        document.getElementById("send").addEventListener('click', (e) => {
            e.preventDefault();
            sendMessage();
        });

    }, false);
