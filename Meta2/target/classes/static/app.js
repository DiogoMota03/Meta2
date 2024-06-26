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
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {

        console.log('Subscribed: ' + frame);

        // Subscribe to the topic
        // The server will send messages to this topic
        stompClient.subscribe('/topic/status', function (message) {
            showMessage(JSON.parse(message.body));
        });
    });
}

/*function changeFormAction() {
    var form = document.getElementById('myForm');
    var switchToggle = document.getElementById('searchToggle');
    if (switchToggle.checked) {
        form.action = "/search";
    } else {
        form.action = "/";
    }
}*/

function changeFormAction() {

    var form = document.getElementById('myForm');
    var switchToggle = document.getElementById('searchToggle');
    var searchBar = document.getElementById("searchText");
    localStorage.setItem('searchText', searchBar.value);

    if (switchToggle.checked) {
        form.action = "/search";
        searchRequest();
    } else {
        form.action = "/";
        // Prevent the form from being submitted normally
        event.preventDefault();

        // Create a FormData object from the form
        var formData = new FormData(form);

        // Use the Fetch API to submit the form
        fetch(form.action, {
            method: form.method,
            body: formData
        })
            .then(response => response.text())
            .then(text => {
                // Show a pop-up with the response text
                alert("Inserting URL <" + text + "> to the queue.");
                searchBar.value = '';
            })
            .catch(error => console.error('Error:', error));
    }
}

function addNewRow(tableID, id, time, repartition) {
    // Get the table by its id
    var table = document.getElementById(tableID);

    // Insert a row at the end of the table
    var newRow = table.insertRow(-1);

    // Insert a cell in the row at index 0
    var newCell1 = newRow.insertCell(0);
    var newCell2 = newRow.insertCell(1);
    var newCell3 = newRow.insertCell(2);

    // Append a text node to the cell
    var newText1 = document.createTextNode(id);
    var newText2 = document.createTextNode(time);
    var newText3 = document.createTextNode(repartition);

    newCell1.appendChild(newText1);
    newCell2.appendChild(newText2);
    newCell3.appendChild(newText3);

    // Change the cell to a header cell
    newCell1.outerHTML = "<th>" + newCell1.innerHTML + "</th>";
    newCell2.outerHTML = "<th>" + newCell2.innerHTML + "</th>";
    newCell3.outerHTML = "<th>" + newCell3.innerHTML + "</th>";
}

/*
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
*/

function status(){
    console.log("------------------------------------");
    var form = document.getElementById('myForm');
    form.action = "/status";
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
    var table = document.getElementById("barrel-table");
    var rows = table.getElementsByTagName("tr");
    while (rows.length > 1) {
        table.deleteRow(1);
    }

    for (var b in message.barrels) {
        addNewRow("barrel-table", message.barrels[b].id, message.barrels[b].time, message.barrels[b].repartition);
    }

    // Manipular elementos frontend
    var messageDiv = document.getElementById("server-message");

    // Update the content of the div
    messageDiv.textContent = message.top10;
}

window.addEventListener('load',
    function () {
        //var page = localStorage.getItem('page') ? parseInt(localStorage.getItem('page')) : 0;
        var searchBar = document.getElementById("searchText");
        var searchBar = document.getElementById("searchText");
        if (searchBar && localStorage.getItem('searchText')) {
                searchBar.value = localStorage.getItem('searchText');
            }
        var searchToggle = document.getElementById("searchToggle");

        /*searchBar.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && searchBar.value !== "") {
                e.preventDefault(); // Prevent form submission
                if (searchToggle.checked)
                    searchRequest();
                else
                    addRequest();
            }
        });
        */


        /*
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
                */
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
                catImage.src = 'cat_inserting.png';
            }
        });




    }, false);
