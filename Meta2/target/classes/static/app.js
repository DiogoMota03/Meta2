var stompClient = null;


function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
    });
}

connect();


// Conjunto de instruções que vai mexer com frontend (acho eu)
/*function setConnected(connected) {
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
}*/

// TODO use for status
function subscribe() {
    var socket = new SockJS('/my-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true); // TODO
        console.log('Subscribed: ' + frame);

        // Subscribe to the topic
        // The server will send messages to this topic
        stompClient.subscribe('/topic/status', function (message) {
            showMessage(JSON.parse(message.body).content);
        });
    });
}


function searchRequest() {
    var searchText = document.getElementById("searchText").value;
    stompClient.send("/app/search", {}, JSON.stringify({'content': searchText}));

    window.location.href = "http://localhost:8080/search?s=" + searchText;
}

function feelingLuckyRequest() {
    //var searchText = document.getElementById("searchText").value;
    //stompClient.send("/app/lucky", {}, ""));

    window.location.href = "http://localhost:8080/lucky";
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
        var searchBar = document.getElementById("searchText");

        searchBar.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && searchBar.value !== "") {
                e.preventDefault(); // Prevent form submission
                searchRequest();
            }
        });

        document.getElementById("search").addEventListener('click', (e) => {
            e.preventDefault();
            if (searchBar.value !== "")
                searchRequest();
        });

        document.getElementById("feelingLucky").addEventListener('click', (e) => {
            e.preventDefault();
            feelingLuckyRequest();
        });
    }, false);
