"use strict";

function Restest(testall = true) {
    let bearer_token = null;
    let tests_ok_count = 0;
    let tests_error_count = 0;
    let token_waiting_list = [];
    let THIS = this;

    this.getErrors = function () {
        return tests_error_count;
    };

    this.getToken = function () {
        return bearer_token;
    };

    let setToken = function (token) {
        bearer_token = token;
        let tokenf = document.getElementById("token-field");
        if (tokenf) {
            tokenf.value = token;
        }
    };

    let extractTokenFromHeader = function (header) {
        return header.substring("Bearer".length).trim();
    };

    let sendRestRequest = function (method, url, callback, acceptType = null, payload = null, payloadType = null, token = null, async = true) {
        let xhr = new XMLHttpRequest();
        xhr.open(method, url, async);
        if (token !== null)
            xhr.setRequestHeader("Authorization", "Bearer " + token);
        if (payloadType !== null)
            xhr.setRequestHeader("Content-Type", payloadType);
        if (acceptType !== null)
            xhr.setRequestHeader("Accept", acceptType);
        if (async) {
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    callback(xhr.responseText, xhr.status, xhr.getResponseHeader("Authorization"));
                }
            };
        }
        xhr.send(payload);
        if (!async) {
            callback(xhr.responseText, xhr.status, xhr.getResponseHeader("Authorization"));
    }
    };


    let makeDefaultResponseCallback = function (params) {
        return function (callResponse, callStatus, callAuthHeader) {
            console.log(params.method + " " + params.url + " response: " + callStatus + ": " + callResponse.substring(0, 50));
            if (params.responseTarget) {
                params.responseTarget.textContent = callStatus + ": " + callResponse;
            }
            if (params.source) {
                params.source.classList.remove("rest-test-wait");
            }
            if ((params.responseStatus === null || (callStatus == params.responseStatus))
                    && (params.responseText === null || (callResponse === params.responseText))) {
                if (params.source) {
                    params.source.classList.add("rest-test-ok");
                    tests_ok_count++;
                }
                if (params.responseHasToken && callAuthHeader !== null) {
                    let token = extractTokenFromHeader(callAuthHeader);
                    console.log("got bearer token: " + token);
                    setToken(token);
                    testAuthItems();
                }
            } else {
                if (params.source) {
                    tests_error_count++;
                    params.source.classList.add("rest-test-error");
                    params.source.setAttribute("data-test-out-response", callResponse);
                    params.source.setAttribute("data-test-out-status", callStatus);
                    params.source.setAttribute("title", callStatus + " " + callResponse);
                }
            }
            if (params.source) {
                if (tests_error_count > 0) {
                    document.body.classList.add("rest-test-error-all");
                    document.body.classList.remove("rest-test-ok-all");
                } else {
                    document.body.classList.remove("rest-test-error-all");
                    document.body.classList.add("rest-test-ok-all");
                }
            }
        };
    };

    let testAuthItems = function () {
        for (let i = 0; i < token_waiting_list.length; ++i) {
            THIS.makeRESTcall(token_waiting_list[i]);
        }
    };

    let loadCredentials = function(string1) {
        if(document.location.href.includes("homepage.html")) {
            let username = string1.split(",")[0];
            let role = string1.split(",")[1];
            if(role === "AMMINISTRATORE")
                document.getElementById("user_image").src = "res/assets/images/xs/boss.png";
            else {
                if(role === "TECNICO") {
                    document.getElementById("user_image").src = "res/assets/images/xs/developer.png";
                } else
                    document.getElementById("user_image").src = "res/assets/images/xs/client.png";
            }
            document.getElementById("username-text").innerHTML = username;
            document.getElementById("role-text").innerHTML = role;
        }
        sendRestRequest(
            "get", "rest/utenti/me",
            function (callResponse, callStatus) {
                if (callStatus === 200) {
                    const user = JSON.parse(callResponse);
                    document.getElementById("idUser").value = user.id;
                    document.getElementById("idUser2").value = user.id;
                    document.getElementById("usernameUser").value = user.username;
                    var indirizzo = user.indirizzo.split(", ");
                    document.getElementById("indirizzoUser").value = indirizzo[0];
                    document.getElementById("numeroUser").value = indirizzo[1];
                    document.getElementById("cittaUser").value = indirizzo[2];
                    document.getElementById("capUser").value = indirizzo[3];
                    let tag = document.querySelector("option[value=\""+indirizzo[4]+"\"]");
                    tag.setAttribute("selected", true);
                }
            },
            null,
            null,
            null,
            bearer_token, false);

        sendRestRequest(
            "get", "rest/utenti/me/gruppo/servizi",
            function (callResponse, callStatus) {
                if (callStatus === 200) {
                    const servizi = JSON.parse(callResponse);
                    servizi.map(element => {
                        if(element.script === "profile")
                            document.getElementById("profileService").removeAttribute("hidden");

                        if(element.script == "notifications")
                            document.getElementById("notificationsService").removeAttribute("hidden");

                        if(element.script == "users")
                            document.getElementById("usersService").removeAttribute("hidden");

                        if(element.script == "newCategory")
                            document.getElementById("createCategory").removeAttribute("hidden");
                    })
                }
            },
            null,
            null,
            null,
            bearer_token, false);
    };

    let createCategory = function() {
        sendRestRequest(
            "get", "rest/categorie",
            function (callResponse, callStatus) {
                if (callStatus === 200) {
                    var categories = JSON.parse(callResponse);
                    editCategory(categories, null);
                }
            },
            null,
            null,
            null,
            bearer_token);
    }

    let editCategory = function(allCategories, choised) {
        if(choised)
            var category = choised;
        else
            var category = {};
        var char = {};
        var listChars;
        var image;
        const formData = new FormData();
        Swal.fire({
            title: "Edita categoria",
            html:`
                <div class="row clearfix">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <input type="hidden" id="id" value="${category.id?category.id:'0'}" required/>
                            <input type="text" id="name" class="form-control" placeholder="Nome categoria" value="${category.nome?category.nome:''}" required/>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <select id="fatherCategory" class="form-control" required>
                                <option value="0" ${!category.categoria_padre ? 'selected' : ''}>Has no father</option>
                                ${allCategories.map(element => {
                                    return `<option value="${element.id}" ${category.categoria_padre && element.id == category.categoria_padre.id ? 'selected' : ''}>${element.nome}</option>`
                                    }).join('')
                                }
                            </select>
                        </div>
                    </div>
                </div>
                <h5 class="card-inside-title">Image of category</h5>
                <p>Upload png or jpg image not larger than 1 Gb</p>
                <div class="row clearfix">
                    ${category.immagine ?
                        `<div class="col-sm-6">
                            <div class="form-group">
                                <img src="rest/immagini/download?id=${category.immagine.id}" alt="${category.immagine.didascalia || 'Image'}">
                            </div>
                        </div>` : ''}
                    <div class="col-sm-6">
                        <div class="form-group">
                            <input type="file" id="imageInsert" accept="image/*" required/>
                        </div>
                    </div>
                </div>
                <h2 class="card-inside-title">Characteristics</h2>
                <div class="row clearfix" id="parentInput">
                    ${category.caratteristiche && category.caratteristiche.length > 0 ? 
                        category.caratteristiche.map(function(element, index) {
                            if(index == 0) {
                                return `
                                <div class="col-sm-4">
                                    <div class="form-group">       
                                        <input type="hidden" name="characteristicKey[]" value="${element.id}">                                                         
                                        <input type="text" name="characteristicName[]" class="form-control" placeholder="Name" value="${element.nome}" required/>                                   
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">    
                                        <input type="text" name="characteristicValue[]" class="form-control" placeholder="Add values separated by ','" pattern="([^,]*[,])*[^,][^,]*" value="${element.valori_default}" required/>                                    
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">    
                                        <button class="btn btn-success" id="d${category.caratteristiche.length}" type="button" onclick="addInput(this)"><i class="zmdi zmdi-plus"></i>Add row</button>                                                       
                                    </div>
                                </div>`
                            } else {
                                return `<div class="col-sm-4" id="a${index}">
                                    <div class="form-group">      
                                        <input type="hidden" name="characteristicKey[]" value="${element.id}">                             
                                        <input type="text" name="characteristicName[]" class="form-control" placeholder="Name" value="${element.nome}" required/>                                   
                                    </div>
                                </div>
                                <div class="col-sm-4" id="b${index}">
                                    <div class="form-group">    
                                        <input type="text" name="characteristicValue[]" class="form-control" placeholder="Add values separated by ','" pattern="([^,]*[,])*[^,][^,]*" value="${element.valori_default}" required/>                                    
                                    </div>
                                </div>
                                <div class="col-sm-4">
                                    <div class="form-group">    
                                        <button class="btn btn-warning" type="button" id="c${index}" onclick="removeInput(this)"><i class="zmdi zmdi-delete"></i> Delete row</button>                                
                                    </div>
                                </div>`
                            }
                        }).join('')
                    : 
                        `<div class="col-sm-4">
                            <div class="form-group">   
                                <input type="hidden" name="characteristicKey[]" value="0">                             
                                <input type="text" name="characteristicName[]" class="form-control" placeholder="Name" required/>                                   
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">    
                                <input type="text" name="characteristicValue[]" class="form-control" placeholder="Add values separated by ','" pattern="([^,]*[,])*[^,][^,]*" required/>                                    
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div class="form-group">    
                                <button class="btn btn-success" id="d1" type="button" onclick="addInput(this)"><i class="zmdi zmdi-plus"></i>Add row</button>                                                       
                            </div>
                        </div>`
                    }

                </div>`,
            focusConfirm: false,
            preConfirm: () => {
                category.id = document.getElementById('id').value;
                category.nome = document.getElementById('name').value;
                if(!category.categoria_padre)
                    category.categoria_padre = {};
                category.categoria_padre.id = document.getElementById('fatherCategory').value ? document.getElementById('fatherCategory').value : 0;
                formData.append("immagine", document.getElementById('imageInsert').files[0]);
                image = document.getElementById('imageInsert').files[0];
                const keys = document.getElementsByName('characteristicKey[]');
                const names = document.getElementsByName('characteristicName[]');
                const values = document.getElementsByName('characteristicValue[]');
                if(!category.caratteristiche)
                    category.caratteristiche = {};
                listChars = "";
                for(let k=0;k<names.length;k++){
                    char.id = keys[k]?keys[k].value:'0';
                    char.nome = names[k].value;
                    char.categoria = {};
                    char.categoria.id = category.id;
                    char.valori_default = values[k].value;
                    listChars += char.id+"ç"+char.nome+"ç"+char.categoria.id+"ç"+char.valori_default+"§";
                }/*
                TODO: da reinserire una volta che il caricamento delle immagini funziona
                if(!category.id || !category.nome || !category.categoria_padre.id || !listChars || !(category.immagine || formData.get("immagine"))) 
                    Swal.showValidationMessage("Campi con valori mancanti");*/
            }
        }).then((result) => {
            if (result.isConfirmed) {
                if(image) {
                    sendRestRequest(
                        "post", "rest/immagini",
                        function (callResponse, callStatus) {
                            if (callStatus === 200) {
                                sendRestRequest(
                                    "post", "rest/categorie",
                                    function (callResponse2, callStatus2) {
                                        if (callStatus2 === 204) {
                                            Swal.fire({title: "Congrats", text: "La categoria è stata modificata", icon: "success"}).then(() => {
                                                handleSeeCategories;
                                            });
                                        } else {
                                            Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                                        }
                                    },
                                    null,
                                    "id="+category.id+"&nome="+category.nome+"&idCategoriaPadre="+category.categoria_padre.id+"&immagine="+callResponse+"&caratteristiche="+listChars,
                                    "application/x-www-form-urlencoded",
                                    bearer_token);
                            } else {
                                Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                            }
                        },
                        null,
                        formData,
                        "multipart/form-data",
                        bearer_token);
                } else {
                    sendRestRequest(
                        "post", "rest/categorie",
                        function (callResponse2, callStatus2) {
                            if (callStatus2 === 204) {
                                Swal.fire({title: "Congrats", text: "La categoria è stata modificata", icon: "success"}).then(() => {
                                    handleSeeCategories;
                                });
                            } else {
                                Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                            }
                        },
                        null,
                        "id="+category.id+"&nome="+category.nome+"&idCategoriaPadre="+category.categoria_padre.id+"&caratteristiche="+listChars,
                        "application/x-www-form-urlencoded",
                        bearer_token);
                }
            }
        });
    }

    function convertTechToUser(event, iduser, bearer_token){
        event.preventDefault();
        Swal.fire({
            title: "Are you sure to convert technician to customer?",
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, convert it!"
        }).then((result) => {
            if (result.isConfirmed) {
            sendRestRequest(
                "post", "rest/utenti/"+iduser+"/assumi?ruolo=ORDINANTE",
                function (callResponse, callStatus) {
                    if (callStatus === 204 || callStatus === 200) {
                        Swal.fire({title: "Congrats", text: "Your technician has been promoted to customer.", icon: "success"}).then(() => {
                            handleSeeUsers;
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null,
                null,
                null,
                bearer_token);
            }
        });
    }
  
    function convertUserToTech(event, iduser, userName, bearer_token){
        event.preventDefault();
        Swal.fire({
            title: "Are you sure to convert customer "+userName+" to technician?",
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, convert it!"
        }).then((result) => {
            if (result.isConfirmed) {
            sendRestRequest(
                "post", "rest/utenti/"+iduser+"/assumi?ruolo=TECNICO",
                function (callResponse, callStatus) {
                    if (callStatus === 204 || callStatus === 200) {
                        Swal.fire({title: "Congrats", text: "Customer has been promoted to technician.", icon: "success"}).then(() => {
                            handleSeeUsers;
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null,
                null,
                null,
                bearer_token);
            }
        });
    }
  
    function convertUserToClient(event, iduser, userName, userEmail, bearer_token){
        event.preventDefault();
        Swal.fire({
            title: "Are you sure to accept user: "+userName+" with email: "+userEmail+" in our system as customer?",
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, grant it!"
        }).then((result) => {
            if (result.isConfirmed) {
            sendRestRequest(
                "post", "rest/utenti/"+iduser+"/accetta",
                function (callResponse, callStatus) {
                    if (callStatus === 204 || callStatus === 200) {
                        Swal.fire({title: "Congrats", text: "Your site has a new client.", icon: "success"}).then(() => {
                            handleSeeUsers;
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null,
                null,
                null,
                bearer_token);
            }
        });
    }
    
    function convertClientToUser(event, iduser, userName, userEmail, bearer_token){
        event.preventDefault();
        Swal.fire({
            title: "Are you sure to refuse user: "+userName+" with email: "+userEmail+" in our system as customer?",
            text: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, refuse it!"
        }).then((result) => {
            if (result.isConfirmed) {
            sendRestRequest(
                "post", "rest/utenti/"+iduser+"/accetta",
                function (callResponse, callStatus) {
                    if (callStatus === 204 || callStatus === 200) {
                        Swal.fire({title: "Congrats", text: "Your site has lost a client.", icon: "success"}).then(() => {
                            handleSeeUsers;
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null,
                null,
                null,
                bearer_token);
            }
        });
    }
    
    function errorTechLocked(event){
        event.preventDefault();
        Swal.fire({
            title: "Oops.. Technician is locked",
            text: "You can't fire technician until has requests active! Wait until they closed",
            icon: "error"
        });
    }

    function cancelRequest(event, reqId, reqTitle, bearer_token){
        event.preventDefault();
        Swal.fire({
            title: "Are you sure to cancel your request: "+reqTitle+"?",
            html: "You won't be able to revert this!",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, cancel it!"
        }).then((result) => {
            if (result.isConfirmed) {
                sendRestRequest(
                    "delete", "rest/richieste/"+reqId,
                    function (callResponse, callStatus) {
                        if (callStatus === 204) {
                            Swal.fire({title: "Congrats", text: "Your request has been cancelled.", icon: "success"}).then(() => {
                                handleSeeOpenRequests;
                            });
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null,
                    null,
                    null,
                    bearer_token);
            }
        });
    }

    function shipOrder(event, reqId, productName, bearer_token){
        event.preventDefault();
        Swal.fire({
            title: "Are you ready to ship order of "+productName+"?",
            text: "Client will be informed about order shipped",
            icon: "success",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, ship it!"
        }).then((result) => {
            if (result.isConfirmed) {
                sendRestRequest(
                    "post", "rest/richieste/"+reqId+"?stato_ordine=SPEDITO",
                    function (callResponse, callStatus) {
                        if (callStatus === 204 || callStatus === 200) {
                            Swal.fire({title: "Shipped!", text: "Order is been labeled as shipped.", icon: "success"}).then(() => {
                                handleSeeShipRequests;
                            });
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null,
                    null,
                    null,
                    bearer_token);
            }
        });
    }
      
    function receivedRequest(event, reqId, title){
        event.preventDefault();
        Swal.fire({
            title: "Is arrived product: "+title+"?",
            html: "Did you accept product?",
            input: 'select',
            inputOptions: {
                'ACCETTATO': 'Accettato',
                'RESPINTONONCONFORME': 'Respinto, non conforme',
                'RESPINTONONFUNZIONANTE': 'Respinto, non funzionante'
            },
            inputPlaceholder: 'required',
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Done!",
            inputValidator: function (value) {
                return new Promise(function (resolve, reject) {
                if (value !== '') {
                    resolve();
                } else {
                    resolve('You must select a option');
                }
                });
            }
            }).then((result) => {
            if (result.isConfirmed) {
                sendRestRequest(
                    "post", "rest/richieste/"+reqId+"?stato_ordine=" + result.value,
                    function (callResponse, callStatus) {
                        if (callStatus === 204 || callStatus === 200) {
                            Swal.fire({title: "Congrats!", text: "Your request has been closed.", icon: "success"}).then(() => {
                                handleSeeShipRequests;
                            });
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null,
                    null,
                    null,
                    bearer_token);
            }
        });
    }

    function notShipRequest(event){
    event.preventDefault();
    Swal.fire({
        title: "Your order has not yet been shipped",
        text: "We will contact you when will be shipped",
        icon: "error"
        });
    }

    function acceptRequest(event, reqId, reqTitle, reqChars, bearer_token){
        event.preventDefault();
        var text = '<strong>Title: </string>'+reqTitle+'<br>';
        if(reqChars) {
            for(let i=0; i<reqChars.length; i++) {
                text += "<strong>" + reqChars[i].caratteristica.nome +  ": </strong>" + reqChars[i].valore;
                if(i+1 < reqChars.length)
                    text += "<br>";
            }
        }
        Swal.fire({
            title: "Are you sure to take request #"+reqId+":",
            html: text,
            icon: "question",
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Yes, accept it!"
        }).then((result) => {
            if (result.isConfirmed) {
                sendRestRequest(
                    "post", "rest/richieste/"+reqId+"/assegna",
                    function (callResponse, callStatus) {
                        if (callStatus === 204 || callStatus === 200) {
                            Swal.fire({title: "Congrats!", text: "This request now is yours!", icon: "success"}).then(() => {
                                handleSeeShipRequests;
                            });
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null,
                    null,
                    null,
                    bearer_token);
            }
        });
    }

    function answerProposal(reqId, propId, bearer_token){
        Swal.fire({
            title: "Do you accept proposal?",
            html: "You won't be able to revert this!",
            input: "radio",
            inputOptions: {"APPROVATO":"Approvato", "RESPINTO": "Respinto"},
            inputValidator: (value) =>{
            if(!value) {
                return "You must choose one";
            }
            },
            showCancelButton: true,
            confirmButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
        }).then((result) => {
            if (result.isConfirmed) {
                if(result.value == "APPROVATO"){
                    sendRestRequest(
                        "post", "rest/richieste/"+reqId+"/proposte/"+propId+"?stato_proposta=APPROVATO",
                        function (callResponse, callStatus) {
                            if (callStatus === 204 || callStatus === 200) {
                                Swal.fire({title: "Thank you!", text: "Your order will be shipped immediatelly!", icon: "success"});
                            } else {
                                Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                            }
                        },
                        null,
                        null,
                        null,
                        bearer_token);
                } else {
                    Swal.fire({
                    title: "We're sorry!",
                    input: "textarea",
                    inputLabel: "Motivation",
                    inputPlaceholder: "Type here your motivation...",
                    showCancelButton: true,
                    confirmButtonColor: "#3085d6",
                    cancelButtonColor: "#d33"
                    }).then((result2) => {
                        if (result2.isConfirmed){
                            sendRestRequest(
                                "post", "rest/richieste/"+reqId+"/proposte/"+propId+"?stato_proposta=RESPINTO&motivazione="+result2.valore,
                                function (callResponse, callStatus) {
                                    if (callStatus !== 204 && callStatus !== 200) {
                                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                                    }
                                },
                                null,
                                null,
                                null,
                                bearer_token);
                        }
                    });
                }
            }
        });
    }

    let populateOpenRequests = function(requestList) {
        const table = document.getElementById("openRequestsTable");
        table.innerHTML = "";
        var requests = JSON.parse(requestList);
        for(let i=0; i < requests.length; i++) {
            if(requests[i].stato_richiesta == "ORDINATO" || requests[i].stato_richiesta == "CHIUSO" || requests[i].stato_richiesta == "ANNULLATO")
                continue;

            var row = document.createElement("tr");
            var cell = document.createElement("td");
            cell.innerText = requests[i].titolo;
            row.appendChild(cell);

            var cell2 = document.createElement("td");
            var description = document.createElement("textarea");
            description.setAttribute("spellcheck", false);
            description.cols = 32;
            description.classList.add("not-split-text");
            description.disabled = true;
            description.value = requests[i].descrizione;
            cell2.appendChild(description);
            row.appendChild(cell2);

            var cell3 = document.createElement("td");
            cell3.innerText = requests[i].categoria.nome;
            row.appendChild(cell3);

            var cell4 = document.createElement("td");
            var ordering = document.createElement("span");
            ordering.classList.add("badge", "badge-success");
            ordering.innerText = requests[i].ordinante;
            cell4.appendChild(ordering);
            row.appendChild(cell4);

            var cell5 = document.createElement("td");
            var technician = document.createElement("span");
            if(requests[i].tecnico) {
                technician.classList.add("badge", "badge-success");
                technician.innerText = requests[i].tecnico;
            } else {
                technician.classList.add("badge", "badge-warning");
                technician.innerText = "No Technician";
            }
            cell5.appendChild(technician);
            row.appendChild(cell5);

            var cell6 = document.createElement("td");
            cell6.innerText = requests[i].stato_richiesta;
            row.appendChild(cell6);

            var cell7 = document.createElement("td");
            if(requests[i].stato_ordine != "EMPTY")
                cell7.innerText = requests[i].stato_ordine;
            row.appendChild(cell7);

            var cell8 = document.createElement("td");
            cell8.innerText = requests[i].data_creazione;
            row.appendChild(cell8);

            var cell9 = document.createElement("td");
            if(document.getElementById("role-text").textContent == "ORDINANTE") {
                var cancella = document.createElement("button");
                cancella.classList.add("btn", "btn-danger", "waves-effect", "waves-float", "btn-sm", "waves-red");
                cancella.addEventListener("click", (event) => { cancelRequest(event, requests[i].id, requests[i].titolo, bearer_token)});  
                var cancellaIcon = document.createElement("i");
                cancellaIcon.classList.add("zmdi", "zmdi-delete");
                cancella.appendChild(cancellaIcon);
                cell9.appendChild(cancella);
            }
            var look = document.createElement("button");
            look.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
            if(document.getElementById("role-text").textContent == "ORDINANTE" && (requests[i].proposta && requests[i].proposta.stato_proposta === "RESPINTO" || !requests[i].proposta))
                look.addEventListener("click", (event) => { event.preventDefault(); showRequest(requests[i], true, bearer_token)});
            else 
                look.addEventListener("click", (event) => { event.preventDefault(); showRequest(requests[i], false, bearer_token)});
            var lookIcon = document.createElement("i");
            lookIcon.classList.add("zmdi", "zmdi-eye");
            look.appendChild(lookIcon); 
            cell9.appendChild(look);
            row.appendChild(cell9);

            table.appendChild(row);
        }
        $(document).ready(function(){
            $('#openTable').DataTable();
        });
    };

    let populateShipRequests = function(requestList) {
        const table = document.getElementById("shipRequestsTable");
        table.innerHTML = "";
        var requests = JSON.parse(requestList);
        for(let i=0; i < requests.length; i++) {
            if(requests[i].stato_richiesta !== "ORDINATO")
                continue;

            var row = document.createElement("tr");
            var cell = document.createElement("td");
            cell.innerText = requests[i].titolo;
            row.appendChild(cell);

            var cell2 = document.createElement("td");
            cell2.innerText = requests[i].categoria.nome;
            row.appendChild(cell2);

            var cell9 = document.createElement("td");
            if(requests[i].proposta)
                cell9.innerText = requests[i].proposta.nome_prodotto;
            row.appendChild(cell9);

            var cell3 = document.createElement("td");
            if(requests[i].proposta) {
                var description = document.createElement("textarea");
                description.setAttribute("spellcheck", false);
                description.cols = 32;
                description.classList.add("not-split-text");
                description.disabled = true;
                description.value = requests[i].proposta.descrizione_prodotto;
                cell3.appendChild(description);
            }
            row.appendChild(cell3);

            var cell4 = document.createElement("td");
            var ordering = document.createElement("span");
            ordering.classList.add("badge", "badge-success");
            ordering.innerText = requests[i].ordinante;
            cell4.appendChild(ordering);
            row.appendChild(cell4);

            var cell6 = document.createElement("td");
            cell6.innerText = requests[i].stato_richiesta;
            row.appendChild(cell6);

            var cell7 = document.createElement("td");
            if(requests[i].stato_ordine != "EMPTY")
                cell7.innerText = requests[i].stato_ordine;
            row.appendChild(cell7);

            var cell8 = document.createElement("td");
            if(document.getElementById("role-text").textContent == "TECNICO") {
                var send = document.createElement("button");
                send.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
                if(requests[i].stato_ordine == "EMPTY" || requests[i].stato_ordine == "") {
                    send.addEventListener("click", (event) => { shipOrder(event, requests[i].id, requests[i].proposta.nome_prodotto, bearer_token)});
                    send.innerText = "Ship";
                } else {
                    send.disabled = true;
                    send.innerText = "Shipped";
                }
                cell8.appendChild(send);
            }
            if(document.getElementById("role-text").textContent == "ORDINANTE") {
                var receive = document.createElement("button");
                receive.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
                if(requests[i].stato_ordine == "SPEDITO" ) {
                    receive.addEventListener("click", (event) => { receivedRequest(event, requests[i].id, requests[i].proposta.nome_prodotto, bearer_token)});
                    receive.innerText = "Collect";
                } else {
                    receive.addEventListener("click", (event) => { notShipRequest(event)});
                    receive.innerText = "Not available";
                }
                cell8.appendChild(receive);
            }
            var look = document.createElement("button");
            look.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
            look.addEventListener("click", (event) => { event.preventDefault(); showRequest(requests[i], false, bearer_token)});
            var lookIcon = document.createElement("i");
            lookIcon.classList.add("zmdi", "zmdi-eye");
            look.appendChild(lookIcon); 
            cell8.appendChild(look);
            row.appendChild(cell8);

            table.appendChild(row);
        }
        $(document).ready(function(){
            $('#shipTable').DataTable();
        });
    };

    let populateCloseRequests = function(requestList) {
        const table = document.getElementById("closeRequestsTable");
        table.innerHTML = "";
        var requests = JSON.parse(requestList);
        for(let i=0; i < requests.length; i++) {
            if(requests[i].stato_richiesta == "NUOVO" || requests[i].stato_richiesta == "PRESOINCARICO" || requests[i].stato_richiesta == "ORDINATO")
                continue;

            var row = document.createElement("tr");
            var cell = document.createElement("td");
            cell.innerText = requests[i].titolo;
            row.appendChild(cell);

            var cell2 = document.createElement("td");
            var description = document.createElement("textarea");
            description.setAttribute("spellcheck", false);
            description.cols = 32;
            description.classList.add("not-split-text");
            description.disabled = true;
            description.value = requests[i].descrizione;
            cell2.appendChild(description);
            row.appendChild(cell2);

            var cell3 = document.createElement("td");
            cell3.innerText = requests[i].categoria.nome;
            row.appendChild(cell3);

            var cell9 = document.createElement("td");
            cell9.innerText = requests[i].proposta.nome_prodotto;
            row.appendChild(cell9);

            var cell4 = document.createElement("td");
            var ordering = document.createElement("span");
            ordering.classList.add("badge", "badge-success");
            ordering.innerText = requests[i].ordinante;
            cell4.appendChild(ordering);
            row.appendChild(cell4);

            var cell5 = document.createElement("td");
            var technician = document.createElement("span");
            if(requests[i].tecnico) {
                technician.classList.add("badge", "badge-success");
                technician.innerText = requests[i].tecnico;
            } else {
                technician.classList.add("badge", "badge-warning");
                technician.innerText = "No Technician";
            }
            cell5.appendChild(technician);
            row.appendChild(cell5);

            var cell6 = document.createElement("td");
            cell6.innerText = requests[i].stato_richiesta;
            row.appendChild(cell6);

            var cell7 = document.createElement("td");
            cell7.innerText = requests[i].stato_ordine;
            row.appendChild(cell7);

            var cell8 = document.createElement("td");
            cell8.innerText = requests[i].data_creazione;
            row.appendChild(cell8);

            var cell9 = document.createElement("td");
            var look = document.createElement("button");
            look.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
            look.addEventListener("click", (event) => { event.preventDefault(); showRequest(requests[i], false, bearer_token)});
            var lookIcon = document.createElement("i");
            lookIcon.classList.add("zmdi", "zmdi-eye");
            look.appendChild(lookIcon); 
            cell9.appendChild(look);
            row.appendChild(cell9);

            table.appendChild(row);
        }
        $(document).ready(function(){
            $('#closeTable').DataTable();
        });
    };

    let populateUnassignedRequests = function(requestList) {
        const table = document.getElementById("unassignedRequestsTable");
        table.innerHTML = "";
        var requests = JSON.parse(requestList);
        for(let i=0; i < requests.length; i++) {

            var row = document.createElement("tr");
            var cell = document.createElement("td");
            cell.innerText = requests[i].titolo;
            row.appendChild(cell);

            var cell2 = document.createElement("td");
            var description = document.createElement("textarea");
            description.setAttribute("spellcheck", false);
            description.cols = 32;
            description.classList.add("not-split-text");
            description.disabled = true;
            description.value = requests[i].descrizione;
            cell2.appendChild(description);
            row.appendChild(cell2);

            var cell3 = document.createElement("td");
            cell3.innerText = requests[i].categoria.nome;
            row.appendChild(cell3);

            var cell4 = document.createElement("td");
            var ordering = document.createElement("span");
            ordering.classList.add("badge", "badge-success");
            ordering.innerText = requests[i].ordinante.username;
            cell4.appendChild(ordering);
            row.appendChild(cell4);

            var cell5 = document.createElement("td");
            cell5.innerText = requests[i].data_creazione;
            row.appendChild(cell5);

            var cell6 = document.createElement("td");
            var scegli = document.createElement("button");
            scegli.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
            scegli.addEventListener("click", (event) => { acceptRequest(event, requests[i].id, requests[i].titolo, requests[i].caratteristiche, bearer_token)});  
            var scegliIcon = document.createElement("i");
            scegliIcon.classList.add("zmdi", "zmdi-assignment-check");
            scegli.appendChild(scegliIcon);
            cell6.appendChild(scegli);
            row.appendChild(cell6);

            table.appendChild(row);
        }
        $(document).ready(function(){
            $('#closeTable').DataTable();
        });
    };

    let createRequest = function(category, bearer_token) {
        sendRestRequest(
            "get", "rest/categorie/"+category.id+"/caratteristiche/albero",
            function (callResponse, callStatus) {
                if (callStatus === 200) {
                    let totalChars = JSON.parse(callResponse);
                    let request = {};
                    request.titolo = "Nuova richiesta";

                    Swal.fire({
                        title: request.titolo,
                        html: 
                            `<table>
                                <thead>
                                    <tr>
                                        <th></th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><label for="stato_richiesta">Stato richiesta:</label></td>
                                        <td>
                                            <input type="text" id="stato_richiesta" name="stato_richiesta" value="NUOVO" disabled>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><label for="stato_ordine">Stato ordine:</label></td>
                                        <td>
                                            <input type="text" id="stato_ordine" name="stato_ordine" value="" disabled>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><label for="categoria">Categoria:</label></td>
                                        <td>
                                            <input type="text" id="categoria" name="categoria" value="${category.nome}" disabled>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><label for="descrizione">Descrizione:</label></td>
                                        <td>
                                            <textarea id="descrizione" name="description" class="swal2-textarea" rows="4" spellcheck="false" class="not-split-text" required></textarea>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2"><label>Caratteristiche: </label></td>
                                    </tr>
                                    ${totalChars.map(element => {
                                        return `
                                            <tr>
                                                <td>
                                                    <input type="hidden" name="caratteristicaKey[]" value="${element.id}">
                                                    <label for="caratteristica${element.id}">${element.nome}: </label>
                                                </td>
                                                <td>
                                                    <select id="caratteristica${element.id}" name="caratteristicaValue[]" class="swal2-select" autocomplete="off" required>
                                                    ${element.valori_default.split(",").map(value => {
                                                        return `<option value="${value}" ${value === "Indifferent"?'selected':''}>${value}</option>`
                                                    }).join('')}
                                                    </select>
                                                <td>
                                            </tr>`
                                    }).join('')}
                                    <tr>
                                        <td><label for="note">Note (opzionale): </label></td>
                                        <td>
                                            <textarea id="note" name="note" class="swal2-textarea not-split-text" rows="4" spellcheck="false"></textarea>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>`,
                        width: 'auto',   
                        showCancelButton: true,
                        confirmButtonColor: "#008000",
                        cancelButtonColor: "#d33",
                        confirmButtonText: "Conferma creazione",
                        preConfirm: () => {
                            request.descrizione = document.getElementById("descrizione").value; 
                            request.categoria = category;
                            request.note = document.getElementById("note").value;
                            const keys = document.getElementsByName('caratteristicaKey[]');
                            const values = document.getElementsByName('caratteristicaValue[]');
                            if(!request.caratteristiche)
                                request.caratteristiche = [];
                            for(let k=0;k<keys.length;k++){
                                let reqChar = {};
                                reqChar.caratteristica = {};
                                reqChar.caratteristica.id = Number(keys[k].value);
                                reqChar.valore = values[k].value;
                                request.caratteristiche.push(reqChar);
                            }

                            if(!request.descrizione || !request.categoria || !request.caratteristiche) 
                                Swal.showValidationMessage("Campi con valori mancanti");
                        }
                    }).then((result) => {
                        if (result.isConfirmed) {
                            sendRestRequest(
                                "put", "rest/richieste",
                                function (callResponse2, callStatus2) {
                                    if (callStatus2 === 204) {
                                        Swal.fire({title: "Congrats", text: "La richiesta è stata creata con successo", icon: "success"});
                                    } else {
                                        Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                                    }
                                },
                                null,
                                JSON.stringify(request),
                                "application/json",
                                bearer_token);
                        }
                    });
                } else {
                    Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                }
            },
            null,
            null,
            "application/x-www-form-urlencoded",
            bearer_token);
    };

    let showRequest = function(passedRequest, editable, bearer_token) {
        sendRestRequest(
            "get", "rest/categorie/"+passedRequest.categoria.id+"/caratteristiche/albero",
            function (callResponse, callStatus) {
                if (callStatus === 200) {
                    let totalChars = JSON.parse(callResponse);
                    let request = passedRequest;
                    if(!request.titolo)
                        request.titolo = "Nuova richiesta";

                    if(request.proposte.length > 0) {
                        Swal.fire({
                            title: request.titolo,
                            html: 
                                `<table>
                                    <thead>
                                        <tr>
                                            <th></th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td><label for="stato_richiesta">Stato richiesta:</label></td>
                                            <td>
                                                <input type="text" id="stato_richiesta" name="stato_richiesta" value="${request.stato_richiesta}" disabled>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label for="stato_ordine">Stato ordine:</label></td>
                                            <td>
                                                <input type="text" id="stato_ordine" name="stato_ordine" value="${request.stato_ordine === 'EMPTY' ? '' : request.stato_ordine}" disabled>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label for="categoria">Categoria:</label></td>
                                            <td>
                                                <input type="text" id="categoria" name="categoria" value="${request.categoria.nome}" disabled>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label for="descrizione">Descrizione:</label></td>
                                            <td>
                                                <textarea id="descrizione" name="description" class="swal2-textarea" rows="4" spellcheck="false" class="not-split-text" ${editable ? '' : 'disabled'}>${request.descrizione ? request.descrizione : ''}</textarea>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2"><label>Caratteristiche: </label></td>
                                        </tr>
                                        ${totalChars.map(element => {
                                            return `
                                                <tr>
                                                    <td>
                                                        <input type="hidden" name="caratteristicaKey[]" value="${element.id}">
                                                        <label for="caratteristica${element.id}">${element.nome}: </label>
                                                    </td>
                                                    <td>
                                                        <select id="caratteristica${element.id}" name="caratteristicaValue[]" class="swal2-select" autocomplete="off" ${editable?'required':'disabled'}>
                                                        ${element.valori_default.split(",").map(value => {
                                                            if(request.caratteristiche) {
                                                                var c = request.caratteristiche.find(item => item.caratteristica.id === element.id);
                                                                if(c)
                                                                    return `<option value="${value}" ${value === c.valore?'selected':''}>${value}</option>`
                                                                else
                                                                    return `<option value="${value}" ${value === "Indifferent"?'selected':''}>${value}</option>`
                                                            } else {
                                                                return `<option value="${value}" ${value === "Indifferent"?'selected':''}>${value}</option>`
                                                            } 
                                                        }).join('')}
                                                        </select>
                                                    <td>
                                                </tr>`
                                        }).join('')}
                                        <tr>
                                            <td><label for="note">Note (opzionale): </label></td>
                                            <td>
                                                <textarea id="note" name="note" class="swal2-textarea not-split-text" rows="4" spellcheck="false" ${editable?'':'disabled'}>${request.note?request.note:''}</textarea>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>`,
                            width: 'auto',   
                            showDenyButton: true,
                            showCancelButton: true,
                            confirmButtonColor: "#008000",
                            denyButtonColor: "#3085d6",
                            cancelButtonColor: "#d33",
                            confirmButtonText: "Conferma",
                            denyButtonText: "Vedi proposte",
                            preConfirm: () => {
                                request.descrizione = document.getElementById("descrizione").value; 
                                request.note = document.getElementById("note").value;
                                const keys = document.getElementsByName('caratteristicaKey[]');
                                const values = document.getElementsByName('caratteristicaValue[]');
                                if(!request.caratteristiche)
                                    request.caratteristiche = [];
                                for(let k=0;k<keys.length;k++){
                                    var rc = request.caratteristiche.find(item => item.caratteristica.id === Number(keys[k].value));
                                    if(rc) {
                                        rc.valore = values[k].value;
                                    } else {
                                        let reqChar = {};
                                        reqChar.caratteristica = {};
                                        reqChar.caratteristica.id = Number(keys[k].value);
                                        reqChar.valore = values[k].value;
                                        request.caratteristiche.push(reqChar);
                                    }
                                }
    
                                if(!request.id || !request.titolo || !request.descrizione || !request.categoria || !request.caratteristiche) 
                                    Swal.showValidationMessage("Campi con valori mancanti");
                            }
                        }).then((result) => {
                            if (result.isConfirmed && editable) {
                                sendRestRequest(
                                    "put", "rest/richieste/"+request.id,
                                    function (callResponse2, callStatus2) {
                                        if (callStatus2 === 204) {
                                            Swal.fire({title: "Congrats", text: "La richiesta è stata modificata", icon: "success"});
                                        } else {
                                            Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                                        }
                                    },
                                    null,
                                    JSON.stringify(request),
                                    "application/json",
                                    bearer_token);
                            }
                            if(result.isDenied){
                                showProposals(request, editable, bearer_token);
                            }
                        });
                    } else {
                        Swal.fire({
                            title: request.titolo,
                            html: 
                                `<table>
                                    <thead>
                                        <tr>
                                            <th></th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td><label for="stato_richiesta">Stato richiesta:</label></td>
                                            <td>
                                                <input type="text" id="stato_richiesta" name="stato_richiesta" value="${request.stato_richiesta}" disabled>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label for="stato_ordine">Stato ordine:</label></td>
                                            <td>
                                                <input type="text" id="stato_ordine" name="stato_ordine" value="${request.stato_ordine === 'EMPTY' ? '' : request.stato_ordine}" disabled>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label for="categoria">Categoria:</label></td>
                                            <td>
                                                <input type="text" id="categoria" name="categoria" value="${request.categoria.nome}" disabled>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><label for="descrizione">Descrizione:</label></td>
                                            <td>
                                                <textarea id="descrizione" name="description" class="swal2-textarea" rows="4" spellcheck="false" class="not-split-text" ${editable ? '' : 'disabled'}>${request.descrizione ? request.descrizione : ''}</textarea>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2"><label>Caratteristiche: </label></td>
                                        </tr>
                                        ${totalChars.map(element => {
                                            return `
                                                <tr>
                                                    <td>
                                                        <input type="hidden" name="caratteristicaKey[]" value="${element.id}">
                                                        <label for="caratteristica${element.id}">${element.nome}: </label>
                                                    </td>
                                                    <td>
                                                        <select id="caratteristica${element.id}" name="caratteristicaValue[]" class="swal2-select" autocomplete="off" ${editable?'required':'disabled'}>
                                                        ${element.valori_default.split(",").map(value => {
                                                            if(request.caratteristiche) {
                                                                var c = request.caratteristiche.find(item => item.caratteristica.id === element.id);
                                                                if(c)
                                                                    return `<option value="${value}" ${value === c.valore?'selected':''}>${value}</option>`
                                                                else
                                                                    return `<option value="${value}" ${value === "Indifferent"?'selected':''}>${value}</option>`
                                                            } else {
                                                                return `<option value="${value}" ${value === "Indifferent"?'selected':''}>${value}</option>`
                                                            } 
                                                        }).join('')}
                                                        </select>
                                                    <td>
                                                </tr>`
                                        }).join('')}
                                        <tr>
                                            <td><label for="note">Note (opzionale): </label></td>
                                            <td>
                                                <textarea id="note" name="note" class="swal2-textarea not-split-text" rows="4" spellcheck="false" ${editable?'':'disabled'}>${request.note?request.note:''}</textarea>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>`,
                            width: 'auto',   
                            showCancelButton: true,
                            confirmButtonColor: "#008000",
                            cancelButtonColor: "#d33",
                            confirmButtonText: "Conferma",
                            preConfirm: () => {
                                request.descrizione = document.getElementById("descrizione").value; 
                                request.note = document.getElementById("note").value;
                                const keys = document.getElementsByName('caratteristicaKey[]');
                                const values = document.getElementsByName('caratteristicaValue[]');
                                if(!request.caratteristiche)
                                    request.caratteristiche = [];
                                for(let k=0;k<keys.length;k++){
                                    var rc = request.caratteristiche.find(item => item.caratteristica.id === Number(keys[k].value));
                                    if(rc) {
                                        rc.valore = values[k].value;
                                    } else {
                                        let reqChar = {};
                                        reqChar.caratteristica = {};
                                        reqChar.caratteristica.id = Number(keys[k].value);
                                        reqChar.valore = values[k].value;
                                        request.caratteristiche.push(reqChar);
                                    }
                                }
    
                                if(!request.id || !request.titolo || !request.descrizione || !request.categoria || !request.caratteristiche) 
                                    Swal.showValidationMessage("Campi con valori mancanti");
                            }
                        }).then((result) => {
                            if (result.isConfirmed && editable) {
                                sendRestRequest(
                                    "put", "rest/richieste/"+request.id,
                                    function (callResponse2, callStatus2) {
                                        if (callStatus2 === 204) {
                                            Swal.fire({title: "Congrats", text: "La richiesta è stata modificata", icon: "success"});
                                        } else {
                                            Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                                        }
                                    },
                                    null,
                                    JSON.stringify(request),
                                    "application/json",
                                    bearer_token);
                            }
                        });
                    }
                } else {
                    Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                }
            },
            null,
            null,
            "application/x-www-form-urlencoded",
            bearer_token);
    };

    let showProposals = function(passedRequest, requestEditable, bearer_token) {
        var request = passedRequest;
        if(request.proposte) {
            var proposals = request.proposte.sort((a, b) => a.id - b.id);
            if(document.getElementById("role-text").textContent == "ORDINANTE" && request.proposta.stato_proposta === "INATTESA") {
                Swal.fire({
                    title: request.titolo,
                    html: 
                    `<ul class="comment-reply list-unstyled">
                        ${proposals.map(element => {
                            return `
                                <li>
                                    <div class="icon-box mr-3"><img class="img-fluid img-thumbnail" src="res/assets/images/xs/developer.png" alt="Awesome Tech"></div>
                                    <div class="text-box">
                                        <h5>${request.tecnico}</h5>
                                        <span class="comment-date mr-3">${element.data_creazione}</span>
                                        ${element.stato_proposta === "INATTESA"?'<span class="replybutton badge mr-3 badge-warning">IN ATTESA</span>':''}
                                        ${element.stato_proposta === "APPROVATO"?'<span class="replybutton badge mr-3 badge-success">'+element.stato_proposta+'</span>':''}
                                        ${element.stato_proposta === "RESPINTO"?'<span class="replybutton badge mr-3 badge-danger">'+element.stato_proposta+'</span>':''}
                                        <p><Strong>Prodotto: </Strong> ${element.nome_prodotto}</p>
                                        <p><Strong>Produttore: </Strong> ${element.nome_produttore}</p>
                                        <p><Strong>Descrizione: </Strong> ${element.descrizione_prodotto}</p>
                                        <p><Strong>Prezzo: </Strong> ${element.prezzo_prodotto} €</p>
                                        <p><Strong>Url: </Strong> ${element.url}</p>
                                        <p><Strong>Note: </Strong> ${element.note}</p>
                                    </div>
                                </li>
                                <hr>
                                ${element.motivazione && element.motivazione.length > 0?
                                    `<li>
                                        <div class="icon-box mr-3"><img class="img-fluid img-thumbnail" src="res/assets/images/xs/client.png" alt="Awesome Client"></div>
                                        <div class="text-box">
                                            <h5>${request.ordinante}</h5>
                                            <p>${element.motivazione}</p>
                                        </div>
                                    </li>`:''}`
                        }).join('<hr>')}
                    </ul>`,
                    width: 'auto',  
                    showDenyButton: true, 
                    showCancelButton: true,
                    confirmButtonColor: "#008000",
                    denyButtonColor: "#3085d6",
                    cancelButtonColor: "#d33",
                    confirmButtonText: "Rispondi alla proposta",
                    denyButtonColor: "Torna alla richiesta"
                }).then((result) => {
                    if (result.isConfirmed)
                        answerProposal(request.id, request.proposta.id, bearer_token);
                    if(result.isDenied)
                        showRequest(request, requestEditable, bearer_token);
                });
            } else {
                if(document.getElementById("role-text").textContent == "TECNICO") {
                    if(request.stato_richiesta == "PRESOINCARICO") {
                        var comandoTecnico;
                        if(request.proposta.stato_proposta === "INATTESA")
                            comandoTecnico = "Modifica proposta";
                        else
                            comandoTecnico = "Crea proposta";

                        Swal.fire({
                            title: request.titolo,
                            html: 
                            `<ul class="comment-reply list-unstyled">
                                ${proposals.map(element => {
                                    return `
                                        <li>
                                            <div class="icon-box mr-3"><img class="img-fluid img-thumbnail" src="res/assets/images/xs/developer.png" alt="Awesome Tech"></div>
                                            <div class="text-box">
                                                <h5>${request.tecnico}</h5>
                                                <span class="comment-date mr-3">${element.data_creazione}</span>
                                                ${element.stato_proposta === "INATTESA"?'<span class="replybutton badge mr-3 badge-warning">IN ATTESA</span>':''}
                                                ${element.stato_proposta === "APPROVATO"?'<span class="replybutton badge mr-3 badge-success">'+element.stato_proposta+'</span>':''}
                                                ${element.stato_proposta === "RESPINTO"?'<span class="replybutton badge mr-3 badge-danger">'+element.stato_proposta+'</span>':''}
                                                <p><Strong>Prodotto: </Strong> ${element.nome_prodotto}</p>
                                                <p><Strong>Produttore: </Strong> ${element.nome_produttore}</p>
                                                <p><Strong>Descrizione: </Strong> ${element.descrizione_prodotto}</p>
                                                <p><Strong>Prezzo: </Strong> ${element.prezzo_prodotto} €</p>
                                                <p><Strong>Url: </Strong> ${element.url}</p>
                                                <p><Strong>Note: </Strong> ${element.note}</p>
                                            </div>
                                        </li>
                                        <hr>
                                        ${element.motivazione && element.motivazione.length > 0?
                                            `<li>
                                                <div class="icon-box mr-3"><img class="img-fluid img-thumbnail" src="res/assets/images/xs/client.png" alt="Awesome Client"></div>
                                                <div class="text-box">
                                                    <h5>${request.ordinante}</h5>
                                                    <p>${element.motivazione}</p>
                                                </div>
                                            </li>`:''}`
                                }).join('<hr>')}
                            </ul>`,
                            width: 'auto',   
                            showDenyButton: true,
                            showCancelButton: true,
                            confirmButtonColor: "#008000",
                            denyButtonColor: "#3085d6",
                            cancelButtonColor: "#d33",
                            confirmButtonText: comandoTecnico,
                            denyButtonText: "Torna alla richiesta"
                        }).then((result) => {
                            if (result.isConfirmed) 
                                editProposal(request, requestEditable, bearer_token);
                            if(result.isDenied)
                                showRequest(request, requestEditable, bearer_token);
                        });
                    } else
                        showGeneralProposals(request, requestEditable, bearer_token);
                } else 
                    showGeneralProposals(request, requestEditable, bearer_token);
            }
        } else {
            if(document.getElementById("role-text").textContent == "TECNICO") {
                Swal.fire({
                    title: "Sorry",
                    text: "Non è stata ancora creata nessuna proposta per questa richiesta",
                    showCancelButton: true,
                    confirmButtonColor: "#3085d6",
                    cancelButtonColor: "#d33",
                    confirmButtonText: "Inserisci proposta"
                  }).then((result) => {
                    if (result.isConfirmed) {
                        editProposal(request, requestEditable, bearer_token);
                    }
                  });
            } else 
                Swal.fire({title: "Sorry", text: "Non è stata ancora creata nessuna proposta per questa richiesta"});
        }
    };
 
    let showGeneralProposals = function(passedRequest, requestEditable, bearer_token) {
        var request = passedRequest;
        if(request.proposte) {
            var proposals = request.proposte.sort((a, b) => a.id - b.id);
            Swal.fire({
                title: request.titolo,
                html: 
                    `<ul class="comment-reply list-unstyled">
                        ${proposals.map(element => {
                            return `
                                <li>
                                    <div class="icon-box mr-3"><img class="img-fluid img-thumbnail" src="res/assets/images/xs/developer.png" alt="Awesome Tech"></div>
                                    <div class="text-box">
                                        <h5>${request.tecnico}</h5>
                                        <span class="comment-date mr-3">${element.data_creazione}</span>
                                        ${element.stato_proposta === "INATTESA"?'<span class="replybutton badge mr-3 badge-warning">IN ATTESA</span>':''}
                                        ${element.stato_proposta === "APPROVATO"?'<span class="replybutton badge mr-3 badge-success">'+element.stato_proposta+'</span>':''}
                                        ${element.stato_proposta === "RESPINTO"?'<span class="replybutton badge mr-3 badge-danger">'+element.stato_proposta+'</span>':''}
                                        <p><Strong>Prodotto: </Strong> ${element.nome_prodotto}</p>
                                        <p><Strong>Produttore: </Strong> ${element.nome_produttore}</p>
                                        <p><Strong>Descrizione: </Strong> ${element.descrizione_prodotto}</p>
                                        <p><Strong>Prezzo: </Strong> ${element.prezzo_prodotto} €</p>
                                        <p><Strong>Url: </Strong> ${element.url}</p>
                                        <p><Strong>Note: </Strong> ${element.note}</p>
                                    </div>
                                </li>
                                <hr>
                                ${element.motivazione && element.motivazione.length > 0?
                                    `<li>
                                        <div class="icon-box mr-3"><img class="img-fluid img-thumbnail" src="res/assets/images/xs/client.png" alt="Awesome Client"></div>
                                        <div class="text-box">
                                            <h5>${request.ordinante}</h5>
                                            <p>${element.motivazione}</p>
                                        </div>
                                    </li>`:''}`
                        }).join('<hr>')}
                    </ul>`,
                width: "50%",
                showCancelButton: true,
                confirmButtonColor: "#008000",
                cancelButtonColor: "#d33",
                confirmButtonText: "Torna alla richiesta",
            }).then((result) => {
                if(result.isConfirmed)
                    showRequest(request, requestEditable, bearer_token);
            });
           
        } else 
            Swal.fire({title: "Sorry", text: "Non è stata ancora creata nessuna proposta per questa richiesta"});
    };

    let editProposal = function(passedRequest, requestEditable, bearer_token) {
        var request = passedRequest;
        var proposal = request.proposta;
        Swal.fire({
            title: request.titolo,
            html: 
                `<table>
                    <thead>
                        <tr>
                            <th></th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td colspan="2">
                                <label>Descrizione: ${request.descrizione}</label><br>
                                <label>Categoria: ${request.categoria.nome}</label><br>
                                ${request.note?'<label>Note: ' + request.note + '</label><br>':''}
                                <label>${request.caratteristiche?'Lista caratteristiche':''}</label><br>
                                <label>
                                    ${request.caratteristiche.map( (element, index) => {
                                        return index + ') ' + element.caratteristica.nome + ': ' + element.valore;
                                    }).join('</label><br><label>')}
                                </label>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><strong>Edita Proposta</strong>
                        </tr>
                        <tr>
                            <td><label for="nome_prodotto">Nome prodotto: </label></td>
                            <td>
                                <input type="text" id="nome_prodotto" name="nome_prodotto" class="swal2-input" value="${proposal?proposal.nome_prodotto:''}" required>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="nome_produttore">Nome produttore: </label></td>
                            <td>
                                <input type="text" id="nome_produttore" name="nome_produttore" class="swal2-input" value="${proposal?proposal.nome_produttore:''}" required>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="descrizione_prodotto">Descrizione prodotto: </label></td>
                            <td>
                                <textarea id="descrizione_prodotto" name="descrizione_prodotto" class="swal2-textarea" rows="4" spellcheck="false" class="not-split-text" required>${proposal?proposal.descrizione_prodotto:''}</textarea>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="prezzo_prodotto">Prezzo prodotto: </label></td>
                            <td>
                                <input type="number" id="prezzo_prodotto" name="prezzo_prodotto" class="swal2-input" min="0.00" max="100000.00" step="0.01" value="${proposal?proposal.prezzo_prodotto:''}" required>
                            </td>
                        </tr>
                        <tr>
                            <td><label for="url">Url: </label></td>
                            <td>
                                <input type="url" id="url" name="url" class="swal2-input" value="${proposal && proposal.url?proposal.url:''}">
                            </td>
                        </tr>
                        <tr>
                            <td><label for="note">Note: </label></td>
                            <td>
                                <textarea id="note" name="note" class="swal2-textarea" rows="4" spellcheck="false" class="not-split-text">${proposal && proposal.note?proposal.note:''}</textarea>
                            </td>
                        </tr>
                    </tbody>
                </table>`,
            width: 'auto',   
            showDenyButton: true,
            showCancelButton: true,
            confirmButtonColor: "#008000",
            denyButtonColor: "#3085d6",
            cancelButtonColor: "#d33",
            confirmButtonText: "Conferma",
            denyButtonText: "Vedi richiesta",
            preConfirm: () => {
                if(!request.proposta) {
                    proposal = {};
                    proposal.richiesta = {};
                    proposal.richiesta.id = request.id;
                }
                proposal.nome_prodotto = document.getElementById("nome_prodotto").value;
                proposal.nome_produttore = document.getElementById("nome_produttore").value;
                proposal.descrizione_prodotto = document.getElementById("descrizione_prodotto").value;
                proposal.prezzo_prodotto = document.getElementById("prezzo_prodotto").value;
                proposal.url = document.getElementById("url").value;
                proposal.note = document.getElementById("note").value;

                if(!proposal.nome_prodotto || !proposal.nome_produttore || !proposal.descrizione_prodotto || !proposal.prezzo_prodotto) 
                    Swal.showValidationMessage("Campi con valori mancanti");
            }
        }).then((result) => {
            if (result.isConfirmed) {
                if(proposal.id) {
                    sendRestRequest(
                        "put", "rest/richieste/"+request.id+"/proposte/"+proposal.id,
                        function (callResponse2, callStatus2) {
                            if (callStatus2 === 204) {
                                Swal.fire({title: "Congrats", text: "La proposta è stata modificata", icon: "success"});
                            } else {
                                Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                            }
                        },
                        null,
                        JSON.stringify(proposal),
                        "application/json",
                        bearer_token);
                } else {
                    sendRestRequest(
                        "put", "rest/richieste/"+request.id+"/proposte",
                        function (callResponse2, callStatus2) {
                            if (callStatus2 === 204) {
                                Swal.fire({title: "Congrats", text: "La proposta è stata creata", icon: "success"});
                            } else {
                                Swal.fire({title: "Sorry", text: callStatus2 + ": " + callResponse2, icon: "warning"});
                            }
                        },
                        null,
                        JSON.stringify(proposal),
                        "application/json",
                        bearer_token);
                }
            }
            if(result.isDenied){
                showRequest(request, requestEditable, bearer_token);
            }
        });
    }
    
    ///////////////////// public object methods

    this.makeRESTcall = function (params, responseCallback = null, async = true) {
        if (responseCallback === null) {
            //default response callback
            responseCallback = makeDefaultResponseCallback(params);
        }
        if (params.source) {
            params.source.classList.add("rest-test-wait");
        }
        console.log("calling " + params.method + " " + params.url + (params.needsAuthorization ? " with token " + bearer_token : "") + (params.responseHasToken ? " (getting bearer token)" : ""));
        sendRestRequest(params.method, params.url, responseCallback, params.acceptType, params.payload, params.payloadType, bearer_token, async);
    };

    this.testAllItems = function () {
        token_waiting_list = [];
        let tl = document.querySelectorAll("[data-rest-test-url], [href][data-rest-test]");
        for (let i = 0; i < tl.length; ++i) {
            let element = tl.item(i);

            let target = null;
            if (element.hasAttribute("data-rest-test-target")) {
                target = element.getAttribute("data-rest-test-target");
                if (target !== "") {
                    target = document.querySelector(target);
                } else {
                    let enclosing_tr = element.closest("tr");
                    if (enclosing_tr !== null && enclosing_tr.querySelector(".output") !== null) {
                        target = enclosing_tr.querySelector(".output");
                    } else {
                        target = null;
                    }
                }
            }

            let params = {
                source: tl.item(i),
                url: element.hasAttribute("href") ? element.getAttribute("href") : element.getAttribute("data-rest-test-url"),
                method: element.hasAttribute("data-rest-test-method") ? element.getAttribute("data-rest-test-method") : "GET",
                payload: element.hasAttribute("data-rest-test-payload") ? element.getAttribute("data-rest-test-payload") : null,
                payloadType: element.hasAttribute("data-rest-test-content-type") ? element.getAttribute("data-rest-test-content-type") : null,
                acceptType: element.hasAttribute("data-rest-test-accept") ? element.getAttribute("data-rest-test-accept") : null,
                needsAuthorization: element.hasAttribute("data-rest-test-auth"),
                responseText: element.hasAttribute("data-rest-test-response") ? element.getAttribute("data-rest-test-response") : null,
                responseStatus: element.hasAttribute("data-rest-test-status") ? element.getAttribute("data-rest-test-status") : 200,
                responseTarget: target,
                responseHasToken: element.hasAttribute("data-rest-test-token")
            };
            if (!params.needsAuthorization || bearer_token !== null) {
                THIS.makeRESTcall(params);
            } else {
                token_waiting_list.push(params);
            }
        }
    };

    /////////////////////

    let handleLoginButton = function () {
        let e = document.getElementById("email-field").value;
        let p = document.getElementById("password-field").value;
        sendRestRequest(
                "post", "rest/auth/login",
                function (callResponse, callStatus, callAuthHeader) {
                    if (callStatus === 200) {
                        setToken(extractTokenFromHeader(callAuthHeader));
                        if(!document.location.href.includes("homepage.html"))
                            document.location.href = "homepage.html";
                    } else {
                        if(callStatus === 409)
                            Swal.fire({title: "Sorry", text: "You're not allowed yet to enter. Wait for cofirmation email", icon: "warning"})
                        else {
                            setToken(null);
                            document.getElementById("wrong_credentials").removeAttribute("hidden");
                        }
                    }
                },
                null,
                "email=" + e + "&password=" + p, "application/x-www-form-urlencoded",
                null);

    };

    let handleLogoutButton = function () {
        sendRestRequest(
                "delete", "rest/auth/logout",
                function (callResponse, callStatus) {
                    if (callStatus === 204) {
                        setToken(null);
                        document.location.href = "login.html";
                    }
                },
                null, null, null, bearer_token);
    };

    let handleRegisterButton = function () {
        let u = document.getElementById("username").value;
        let e = document.getElementById("email").value;
        let p = document.getElementById("password").value;
        let a = document.getElementById("address").value;
        let n = document.getElementById("number").value;
        let c = document.getElementById("city").value;
        let k = document.getElementById("cap").value;
        let o = document.getElementById("country").value;
        if(u || e || p || a || n || c || k || o) {
            sendRestRequest(
                "post", "rest/auth/register",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        Swal.fire({title: "Congrats", text: "Registration with success. You'll receive an e-mail when authorized to enter!", icon: "success"}).then(() => {
                            document.location.href = "login.html";
                        });
                    } else {
                        if(callStatus === 409)
                            document.getElementById("email_error").removeAttribute("hidden");
                        else
                            setToken(null);
                    }
                },
                null,
                "username=" + u + "&email=" + e + "&password=" + p + "&address=" + a + "&number=" + n + "&city=" + c + "&cap=" + k + "&country=" + o, "application/x-www-form-urlencoded",
                null);
        } else {
            document.getElementById("parameters_error").removeAttribute("hidden");
        }

    };

    let handleRefreshButton = function () {
        sendRestRequest(
                "get", "rest/auth/refresh",
                function (callResponse, callStatus, callAuthHeader) {
                    if (callStatus === 200) {
                        setToken(extractTokenFromHeader(callAuthHeader));
                        loadCredentials(callResponse);
                    } else {
                        if (callStatus === 401) {
                            Swal.fire({title: "Sorry", text: "Login losed. Retry to access!", icon: "error"}).then(() => {
                                document.location.href = "login.html";
                            });
                        }
                        setToken(null);
                    }
                },
                null, null, null, bearer_token);

    };
    
    let handleEditProfileButton = function () {
        let id = document.getElementById("idUser").value;
        let username = document.getElementById("usernameUser").value;
        let indirizzo = document.getElementById("indirizzoUser").value;
        let numero = document.getElementById("numeroUser").value;
        let citta = document.getElementById("cittaUser").value;
        let cap = document.getElementById("capUser").value;
        let nazione = document.getElementById("nazioneUser").value;
        if(id && username && indirizzo && numero && cap && nazione) {
            sendRestRequest(
                "post", "rest/utenti/modifica",
                function (callResponse, callStatus) {
                    if (callStatus === 204) {
                        Swal.fire({title: "Congrats", text: "Le informazioni dell'account sono state aggiornate con successo", icon: "success"}).then(() => {
                            handleRefreshButton;
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null,
                "id=" + id + "&username=" + username + "&indirizzo=" + indirizzo + "&numero=" + numero + "&citta=" + citta + "&cap=" + cap + "&nazione=" + nazione + "&tipo=1", 
                "application/x-www-form-urlencoded",
                bearer_token);
        } else {
            Swal.fire({title: "Sorry", text: "I dati del profilo sono mancanti", icon: "warning"});
        }
    };

    let handleEditProfile2Button = function () {
        let id = document.getElementById("idUser2").value;
        let email = document.getElementById("emailUser").value;
        let password = document.getElementById("passwordUser").value;
        let password2 = document.getElementById("passwordUser2").value;
        if(password != password2) {
            if(id && (email || password || password2)) {
                sendRestRequest(
                    "post", "rest/utenti/modifica",
                    function (callResponse, callStatus) {
                        if (callStatus === 204) {
                            Swal.fire({title: "Congrats", text: "Le credenziali di sicurezza sono state cambiate con successo! \nOra verrai scollegato per ripetere l'accesso", icon: "success"}).then(() => {
                                handleLogoutButton;
                            });
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, "id=" + id + "&email=" + email + "&currentPassword=" + password + "&newPassword=" + password2 + "&tipo=2", "application/x-www-form-urlencoded", bearer_token);
            } else {
                Swal.fire({title: "Sorry", text: "Nessun parametro da cambiare", icon: "warning"});
            }
        } else
            Swal.fire({title: "Sorry", text: "Le password corrispondono, cambia la nuova password", icon: "warning"});
    };

    let handleSeeNotifications = function () {
        sendRestRequest(
                "get", "rest/utenti/me/notifiche",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        const table = document.getElementById("notificationsTable");
                        table.innerHTML = "";
                        var notifications = JSON.parse(callResponse);
                        for(let i=0; i < notifications.length; i++) {
                            var row = document.createElement("tr");
                            var cell = document.createElement("td");
                            if(!notifications[i].letto) {
                                var lettura = document.createElement("button");
                                lettura.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
                                lettura.addEventListener("click", () => {    
                                    sendRestRequest(
                                    "post", "rest/utenti/me/notifiche?id=" + notifications[i].id,
                                    function (callResponse, callStatus) {
                                        if (callStatus === 204) {
                                            Swal.fire({title: "Congrats", text: "La notifica è stata segnata come letta", icon: "success"}).then(() => {
                                                handleSeeNotifications;
                                            });
                                        } else {
                                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                                        }
                                    },
                                    null,
                                    null,
                                    null,
                                    bearer_token);
                                });
                                var letturaIcon = document.createElement("i");
                                letturaIcon.classList.add("zmdi", "zmdi-markunread-mailbox");
                                lettura.appendChild(letturaIcon);
                                cell.appendChild(lettura);
                            } else {
                                var lettura = document.createElement("button");
                                lettura.classList.add("btn", "btn-warning", "waves-effect", "waves-float", "btn-sm", "waves-amber");
                                lettura.addEventListener("click", () => {    
                                    sendRestRequest(
                                    "post", "rest/utenti/me/notifiche?id=" + notifications[i].id,
                                    function (callResponse, callStatus) {
                                        if (callStatus === 204) {
                                            Swal.fire({title: "Congrats", text: "La notifica è stata segnata come non letta", icon: "success"}).then(() => {
                                                handleSeeNotifications;
                                            });
                                        } else {
                                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                                        }
                                    },
                                    null,
                                    null,
                                    null,
                                    bearer_token);
                                });
                                var letturaIcon = document.createElement("i");
                                letturaIcon.classList.add("zmdi", "zmdi-eye-off");
                                lettura.appendChild(letturaIcon);
                                cell.appendChild(lettura);
                            }
                            var cancellazione = document.createElement("button");
                            cancellazione.classList.add("btn", "btn-danger", "waves-effect", "waves-float", "btn-sm", "waves-red");
                            cancellazione.addEventListener("click", () => {    
                                sendRestRequest(
                                "delete", "rest/utenti/me/notifiche?id=" + notifications[i].id,
                                function (callResponse, callStatus) {
                                    if (callStatus === 204) {
                                        Swal.fire({title: "Congrats", text: "La notifica è stata cancellata", icon: "success"}).then(() => {
                                            handleSeeNotifications;
                                        });
                                    } else {
                                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                                    }
                                },
                                null,
                                null,
                                null,
                                bearer_token);
                            });
                            var cancellazioneIcon = document.createElement("i");
                            cancellazioneIcon.classList.add("zmdi", "zmdi-delete");
                            cancellazione.appendChild(cancellazioneIcon);
                            cell.appendChild(cancellazione);
                            row.appendChild(cell);

                            var cell3 = document.createElement("td");
                            var tipo = document.createElement("span");
                            switch(notifications[i].tipo) {
                                case "INFO":
                                    tipo.classList.add("icon-circle", "bg-blue", "waves-effect", "waves-float", "btn-sm", "waves-blue");
                                    var tipoIcon = document.createElement("i");
                                    tipoIcon.classList.add("zmdi", "zmdi-account");
                                    tipo.appendChild(tipoIcon);
                                break;
                                case "NUOVO":
                                    tipo.classList.add("icon-circle", "bg-green", "waves-effect", "waves-float", "btn-sm", "waves-green");
                                    var tipoIcon = document.createElement("i");
                                    tipoIcon.classList.add("zmdi", "zmdi-comment-text-alt");
                                    tipo.appendChild(tipoIcon);
                                break;
                                case "MODIFICATO":
                                    tipo.classList.add("icon-circle", "bg-amber", "waves-effect", "waves-float", "btn-sm", "waves-amber");
                                    var tipoIcon = document.createElement("i");
                                    tipoIcon.classList.add("zmdi", "zmdi-edit");
                                    tipo.appendChild(tipoIcon);
                                break;
                                case "CHISO":
                                    tipo.classList.add("icon-circle", "bg-purple", "waves-effect", "waves-float", "btn-sm", "waves-purple");
                                    var tipoIcon = document.createElement("i");
                                    tipoIcon.classList.add("zmdi", "zmdi-shopping-chart");
                                    tipo.appendChild(tipoIcon);
                                break;
                                case "ANNULLATO":
                                    tipo.classList.add("icon-circle", "bg-red", "waves-effect", "waves-float", "btn-sm", "waves-red");
                                    var tipoIcon = document.createElement("i");
                                    tipoIcon.classList.add("zmdi", "zmdi-delete");
                                    tipo.appendChild(tipoIcon);
                                break;
                            }
                            cell3.appendChild(tipo);
                            row.appendChild(cell3);

                            var cell4 = document.createElement("td");
                            var testo = document.createTextNode(notifications[i].messaggio);
                            cell4.appendChild(testo);
                            row.appendChild(cell4);

                            var cell5 = document.createElement("td");
                            var dataIcon = document.createElement("i");
                            dataIcon.classList.add("zmdi", "zmdi-time");
                            cell5.appendChild(dataIcon);
                            var data = document.createTextNode(" " + notifications[i].data_creazione);
                            cell5.appendChild(data);
                            row.appendChild(cell5);

                            var cell6 = document.createElement("td");
                            var link = document.createTextNode(notifications[i].link);
                            cell6.appendChild(link);
                            row.appendChild(cell6);

                            table.appendChild(row);
                        }
                        $(document).ready(function(){
                            $('#notificationTable').DataTable();
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
    };

    let handleSeeUsers = function () {
        sendRestRequest(
                "get", "rest/utenti",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        const table = document.getElementById("usersTable");
                        table.innerHTML = "";
                        var users = JSON.parse(callResponse);
                        for(let i=0; i < users.length; i++) {
                            var row = document.createElement("tr");
                            var cell = document.createElement("td");
                            var immagine = document.createElement("img");
                            immagine.classList.add("rounded", "avatar");
                            if(users[i].ruolo == "TECNICO") {
                                immagine.src = "res/assets/images/xs/developer.png"
                                immagine.alt = "User"
                            } else {
                                immagine.src = "res/assets/images/xs/client.png"
                                immagine.alt = "Tech"
                            }
                            cell.appendChild(immagine);
                            row.appendChild(cell);

                            var cell2 = document.createElement("td");
                            var badge = document.createElement("span");
                            badge.classList.add("badge", "badge-primary");
                            badge.textContent = users[i].username;
                            var email = document.createElement("small");
                            email.innerHTML = users[i].email;
                            cell2.appendChild(badge);
                            cell2.innerHTML += "<br>";
                            cell2.appendChild(email);
                            row.appendChild(cell2);

                            var cell3 = document.createElement("td");
                            let address = users[i].indirizzo.split(", ");
                            cell3.innerHTML = address[0] + ", " + address[1] + "<br>" + address[2] + ", " + address[3] + "<br>";
                            var city = document.createElement("small");
                            city.innerHTML = address[4];
                            cell3.appendChild(city);
                            row.appendChild(cell3);
                            
                            var cell4 = document.createElement("td");
                            var activeRequests = document.createElement("strong");
                            activeRequests.innerHTML = users[i].richieste_attive;
                            cell4.appendChild(activeRequests);
                            row.appendChild(cell4);

                            var cell5 = document.createElement("td");
                            var registred = document.createElement("strong");
                            registred.innerHTML = users[i].data_iscrizione;
                            cell5.appendChild(registred);
                            row.appendChild(cell5);

                            var cell6 = document.createElement("td");
                            var status = document.createElement("span");
                            if(users[i].ruolo == "TECNICO") {
                                if(users[i].richieste_attive > 0) {
                                    status.classList.add("badge", "badge-danger");
                                    status.textContent = "Locked";
                                } else {
                                    status.classList.add("badge", "badge-success");
                                    status.textContent = "Unlocked";
                                }
                            } else {
                                if(!users[i].accettato) {
                                    status.classList.add("badge", "badge-danger");
                                    status.textContent = "Not accepted";
                                } else {
                                    status.classList.add("badge", "badge-success");
                                    status.textContent = "Accepted";
                                }
                            }
                            cell6.appendChild(status);
                            row.appendChild(cell6);

                            var cell7 = document.createElement("td");
                            var accept = document.createElement("button");
                            accept.classList.add("btn", "btn-primary");
                            if(users[i].ruolo == "TECNICO") {
                                accept.setAttribute("disabled", true);
                                accept.innerHTML = "Accettato";
                            } else {
                                if(users[i].accettato) {
                                    accept.addEventListener("click", (event) => { convertClientToUser(event, users[i].id, users[i].username, users[i].email, bearer_token)});
                                    accept.innerHTML = "Rifiuta cliente";
                                } else {
                                    accept.addEventListener("click", (event) => { convertUserToClient(event, users[i].id, users[i].username, users[i].email, bearer_token)});
                                    accept.innerHTML = "Accetta utente";
                                    if(users[i].richieste_attive > 0) {
                                        accept.setAttribute("disabled", true);
                                    }
                                }
                            }
                            cell7.appendChild(accept);
                            row.appendChild(cell7);

                            var cell8 = document.createElement("td");
                            var assume = document.createElement("button");
                            assume.classList.add("btn", "btn-primary");
                            if(users[i].ruolo == "TECNICO") {
                                if(users[i].richieste_attive > 0) {
                                    assume.addEventListener("click", (event) => { errorTechLocked(event)});
                                    assume.setAttribute("disabled", true);
                                } else {
                                    assume.addEventListener("click", (event) => { convertTechToUser(event, users[i].id, bearer_token)});
                                }
                                assume.innerHTML = "Licenzia";
                            } else {
                                assume.addEventListener("click", (event) => { convertUserToTech(event, users[i].id, users[i].username, bearer_token)});
                                assume.innerHTML = "Assumi";
                                if(users[i].richieste_attive > 0 || !users[i].accettato) {
                                    assume.setAttribute("disabled", true);
                                }
                            }
                            cell8.appendChild(assume);
                            row.appendChild(cell8);

                            table.appendChild(row);
                        }
                        $(document).ready(function(){
                            $('#userTable').DataTable();
                        });
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
    };

    let handleSeeCategories = function () {
        sendRestRequest(
                "get", "rest/categorie",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        const table = document.getElementById("categoriesTable");
                        table.innerHTML = "";
                        var categories = JSON.parse(callResponse);
                        for(let i=0; i < categories.length; i++) {
                            var row = document.createElement("tr");
                            row.id = "node-" + categories[i].id;
                            if(categories[i].categoria_padre != null && categories[i].categoria_padre.id > 0)
                                row.classList.add("child-of-node-" + categories[i].categoria_padre.id);
                            var cell = document.createElement("td");
                            row.appendChild(cell);

                            var cell2 = document.createElement("td");
                            var immagine = document.createElement("img");
                            immagine.classList.add("width-50");
                            immagine.src = "rest/immagini/download?id=" + categories[i].immagine.id;
                            cell2.appendChild(immagine);
                            row.appendChild(cell2);

                            var cell3 = document.createElement("td");
                            var title = document.createTextNode(categories[i].nome);
                            cell3.appendChild(title);
                            row.appendChild(cell3);

                            var cell4 = document.createElement("td");
                            var characteristics = document.createElement("span");
                            characteristics.classList.add("text-muted");
                            for(let j=0; j < categories[i].caratteristiche.length; j++) {
                                characteristics.innerHTML += categories[i].caratteristiche[j].nome + ": " + categories[i].caratteristiche[j].valori_default.replaceAll(",", ", ");
                                if(j < categories[i].caratteristiche.length)
                                    characteristics.innerHTML += "<br>";
                            }
                            cell4.appendChild(characteristics);
                            row.appendChild(cell4);

                            var cell5 = document.createElement("td");
                            if(document.getElementById("role-text").textContent == "AMMINISTRATORE") {
                                var edita = document.createElement("button");
                                edita.classList.add("btn", "btn-warning", "waves-effect", "waves-float", "btn-sm", "waves-amber");
                                edita.addEventListener("click", () => {  
                                    editCategory(categories, categories[i]);
                                });
                                var editaIcon = document.createElement("i");
                                editaIcon.classList.add("zmdi", "zmdi-edit");
                                edita.appendChild(editaIcon);
                                cell5.appendChild(edita);

                                var cancella = document.createElement("button");
                                cancella.classList.add("btn", "btn-danger", "waves-effect", "waves-float", "btn-sm", "waves-red");
                                cancella.addEventListener("click", () => {    
                                    Swal.fire({
                                        title: "Warning", 
                                        text: "Sei sicuro di voler cancellare la categoria " + categories[i].nome + "?", 
                                        icon: "question",
                                        showCancelButton: true,
                                        confirmButtonColor: "#3085d6",
                                        cancelButtonColor: "#d33",
                                        confirmButtonText: "Si, cancella!",
                                        cancelButtonText: "Annulla"
                                    }).then((result) => {
                                        if (result.isConfirmed) {
                                            sendRestRequest(
                                                "delete", "rest/categorie?id=" + categories[i].id,
                                                function (callResponse, callStatus) {
                                                    if (callStatus === 204) {
                                                        Swal.fire({title: "Congrats", text: "La categoria è stata cancellata", icon: "success"}).then(() => {
                                                            handleSeeCategories;
                                                        });
                                                    } else {
                                                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                                                    }
                                                },
                                                null,
                                                null,
                                                null,
                                                bearer_token);
                                        }
                                    });
                                });
                                var cancellaIcon = document.createElement("i");
                                cancellaIcon.classList.add("zmdi", "zmdi-delete");
                                cancella.appendChild(cancellaIcon);
                                cell5.appendChild(cancella);
                            } else {
                                if(document.getElementById("role-text").textContent == "ORDINANTE") {
                                    var scelta = document.createElement("button");
                                    scelta.classList.add("btn", "btn-success", "waves-effect", "waves-float", "btn-sm", "waves-green");
                                    scelta.addEventListener("click", () => {    
                                        createRequest(categories[i], bearer_token);
                                    });
                                    var sceltaIcon = document.createElement("i");
                                    sceltaIcon.classList.add("zmdi", "zmdi-plus");
                                    scelta.appendChild(sceltaIcon);
                                    scelta.innerText += "Crea richiesta";
                                    cell5.appendChild(scelta);
                                }
                            }
                            row.appendChild(cell5);

                            table.appendChild(row);
                        }
                        $(document).ready(function()  {
                            $("#tree").treeTable();
                          });
                        $(document).ready(function(){
                            $('#tree').DataTable();
                        });/*
                        if(document.getElementById("role-text").textContent == "AMMINISTRATORE")
                            document.getElementById("createCategory").removeAttribute("hidden");*/
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
    };

    let handleSeeOpenRequests = function () {
        if(document.getElementById("role-text").textContent == "AMMINISTRATORE") {
            sendRestRequest(
                "get", "rest/richieste",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        populateOpenRequests(callResponse);
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
        } else 
            if(document.getElementById("role-text").textContent == "TECNICO") {
                document.getElementById("unassignedView").removeAttribute("hidden");
                sendRestRequest(
                    "get", "rest/richieste?tecnico=" + document.getElementById("idUser").value,
                    function (callResponse, callStatus) {
                        if (callStatus === 200) {
                            populateOpenRequests(callResponse);
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, null, null, bearer_token);
            } else {
                sendRestRequest(
                    "get", "rest/richieste?ordinante=" + document.getElementById("idUser").value,
                    function (callResponse, callStatus) {
                        if (callStatus === 200) {
                            populateOpenRequests(callResponse);
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, null, null, bearer_token);
            }
    };

    let handleSeeShipRequests = function () {
        if(document.getElementById("role-text").textContent == "AMMINISTRATORE") {
            sendRestRequest(
                "get", "rest/richieste",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        populateShipRequests(callResponse);
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
        } else 
            if(document.getElementById("role-text").textContent == "TECNICO") {
                sendRestRequest(
                    "get", "rest/richieste?tecnico=" + document.getElementById("idUser").value,
                    function (callResponse, callStatus) {
                        if (callStatus === 200) {
                            populateShipRequests(callResponse);
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, null, null, bearer_token);
            } else {
                sendRestRequest(
                    "get", "rest/richieste?ordinante=" + document.getElementById("idUser").value,
                    function (callResponse, callStatus) {
                        if (callStatus === 200) {
                            populateShipRequests(callResponse);
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, null, null, bearer_token);
            }
    };

    let handleSeeCloseRequests = function () {
        if(document.getElementById("role-text").textContent == "AMMINISTRATORE") {
            sendRestRequest(
                "get", "rest/richieste",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        populateCloseRequests(callResponse);
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
        } else 
            if(document.getElementById("role-text").textContent == "TECNICO") {
                sendRestRequest(
                    "get", "rest/richieste?tecnico=" + document.getElementById("idUser").value,
                    function (callResponse, callStatus) {
                        if (callStatus === 200) {
                            populateCloseRequests(callResponse);
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, null, null, bearer_token);
            } else {
                sendRestRequest(
                    "get", "rest/richieste?ordinante=" + document.getElementById("idUser").value,
                    function (callResponse, callStatus) {
                        if (callStatus === 200) {
                            populateCloseRequests(callResponse);
                        } else {
                            Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        }
                    },
                    null, null, null, bearer_token);
            }
    };

    let handleSeeUnassignedRequests = function () {
        if(document.getElementById("role-text").textContent == "TECNICO") {
            sendRestRequest(
                "get", "rest/richieste/nonassegnate",
                function (callResponse, callStatus) {
                    if (callStatus === 200) {
                        populateUnassignedRequests(callResponse);
                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                        document.getElementById("unassignedRequestsTable").innerHTML = "";
                    }
                },
                null, null, null, bearer_token);
        }
    };

    /////////////////////

    let init = function () {
        //bind login/logout/register/refresh buttons, if present
        let token_field = document.getElementById("token-field");
        let username_field = document.getElementById("username-text");

        let loginb = document.getElementById("login-button");
        if (loginb)
            loginb.addEventListener("click", function (e) {
                handleLoginButton();
                e.preventDefault();
            });
        let logoutb = document.getElementById("logout-button");
        if (logoutb)
            logoutb.addEventListener("click", function (e) {
                handleLogoutButton();
                e.preventDefault();
            });
        let registerb = document.getElementById("register-button");
        if (registerb)
            registerb.addEventListener("click", function (e) {
                handleRegisterButton();
                e.preventDefault();
            });
        let refreshb = document.getElementById("refresh-button");
        if (refreshb || (token_field != null && token_field.value))
            refreshb.addEventListener("click", function (e) {
                handleRefreshButton();
                e.preventDefault();
            });
        let editProfileb = document.getElementById("editProfile-button");
        if (editProfileb)
            editProfileb.addEventListener("click", function (e) {
                handleEditProfileButton();
                e.preventDefault();
            });
        let editProfileb2 = document.getElementById("editProfile-button2");
        if (editProfileb2)
            editProfileb2.addEventListener("click", function (e) {
                handleEditProfile2Button();
                e.preventDefault();
            });
        let seeNotifications = document.getElementById("notificationsView");
        if (seeNotifications)
            seeNotifications.addEventListener("click", function (e) {
                handleSeeNotifications();
                e.preventDefault();
            });
        let seeUsers = document.getElementById("usersView");
        if (seeUsers)
            seeUsers.addEventListener("click", function (e) {
                handleSeeUsers();
                e.preventDefault();
            });
        let seeCategories = document.getElementById("categoriesView");
        if (seeCategories)
            seeCategories.addEventListener("click", function (e) {
                handleSeeCategories();
                e.preventDefault();
            });
        let newCategory = document.getElementById("createCategory");
        if (newCategory)
            newCategory.addEventListener("click", function (e) {
                createCategory();
                e.preventDefault();
            });
        let seeRequests = document.getElementById("requestsView");
        if (seeRequests)
            seeRequests.addEventListener("click", function (e) {
                handleSeeOpenRequests();
                e.preventDefault();
            });
        let seeOpenRequests = document.getElementById("openRequestView");
        if (seeOpenRequests)
            seeOpenRequests.addEventListener("click", function (e) {
                handleSeeOpenRequests();
                e.preventDefault();
            });
        let shopRequestView = document.getElementById("shipRequestView");
        if (shopRequestView)
            shopRequestView.addEventListener("click", function (e) {
                handleSeeShipRequests();
                e.preventDefault();
            });
        let seeCloseRequests = document.getElementById("closeRequestView");
        if (seeCloseRequests)
            seeCloseRequests.addEventListener("click", function (e) {
                handleSeeCloseRequests();
                e.preventDefault();
            });    
        let seeUnassignedRequests = document.getElementById("unassignedRequestView");
        if (seeUnassignedRequests)
            seeUnassignedRequests.addEventListener("click", function (e) {
                handleSeeUnassignedRequests();
                e.preventDefault();
            }); 
        if ((token_field != null && !token_field.value) ||
            (username_field != null && !username_field.value)) {
            handleRefreshButton();
        };


        //modify the <a> links that need an authorization header
        let nl = document.querySelectorAll("a[href][data-rest-test-auth]");
        for (let i = 0; i < nl.length; ++i) {
            let anchor = nl.item(i);
            anchor.addEventListener("click", function (e) {
                sendRestRequest(
                        "get", anchor.href,
                        function (callResponse, callStatus) {
                            if (callStatus === 200) {
                                let myWindow = window.open('data:application/json,'+encodeURIComponent(callResponse), "_blank");
                                myWindow.focus();
                            } else {
                                Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                            }
                        },
                        null, null, null, bearer_token, false);
               e.preventDefault();
            });
        }

        if (testall) {
            THIS.testAllItems();
        }
    };

    init();
}
