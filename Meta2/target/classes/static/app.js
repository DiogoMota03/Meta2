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

function addRequest() {
    var searchText = document.getElementById("searchText").value;
    stompClient.send("/app/insert", {}, JSON.stringify({'content': searchText}));
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
        var searchToggle = document.getElementById("searchToggle");

        searchBar.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && searchBar.value !== "") {
                e.preventDefault(); // Prevent form submission
                if (searchToggle.checked)
                    searchRequest();
                else
                    addRequest();
            }
        });

        document.getElementById("search").addEventListener('click', (e) => {
            e.preventDefault();
            if (searchBar.value !== "")
                if (searchToggle.checked)
                    searchRequest();
                else
                    addRequest();
        });

        document.getElementById("feelingLucky").addEventListener('click', (e) => {
            e.preventDefault();
            feelingLuckyRequest();
        });
        // Select the .cat element
        var cat = document.querySelector('.cat');

        // Define the event listener function
        var animationEndHandler = function() {
            cat.style.zIndex = '1';

            cat.style.animation = 'slide-down 2s forwards';
        };

        cat.addEventListener('animationend', animationEndHandler);

        var logoClickHandler = function(e) {
            e.preventDefault();
            // cat animation
            cat.style.animation = 'slide-up 2s forwards';

            // Remove the click event listener from the logo
            document.getElementById("logo").removeEventListener('click', logoClickHandler);
        };

        // Add the click event listener to the logo
        document.getElementById("logo").addEventListener('click', logoClickHandler);

        searchToggle.addEventListener('change', function () {
            var searchText = document.getElementById("searchText");
            var catImage = document.querySelector('.cat img');
            if (searchToggle.checked) {
                searchText.placeholder = 'Search...';
                catImage.src = 'cat_searching.png';
            } else {
                searchText.placeholder = 'Insert...';
                catImage.src = 'car_inserting.png';
            }
        });

    }, false);
