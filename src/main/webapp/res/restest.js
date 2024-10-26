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
          else
            if(role === "TECNICO")
              document.getElementById("user_image").src = "res/assets/images/xs/developer.png";
            else
              document.getElementById("user_image").src = "res/assets/images/xs/client.png";
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
    };
/*
    let loadServices = function() {
        sendRestRequest(
            "get", "rest/servizio"
        )
    }*/
    
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
                            row.appendChild(cell);

                            var cell2 = document.createElement("td");
                            var cancellazione = document.createElement("button");
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
                            cell2.appendChild(cancellazione);
                            row.appendChild(cell2);

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
                            row.appendChild(cell2);

                            var cell3 = document.createElement("td");
                            var title = document.createTextNode(categories[i].nome);
                            row.appendChild(cell3);

                            var cell4 = document.createElement("td");
                            var characteristics = document.createElement("span");
                            characteristics.classList.add("text-muted");
                            for(let j=0; j < categories[i].caratteristiche.length; j++) {
                                characteristics.textContent.concat(categories[i].caratteristiche[j].nome, ": ", categories[i].caratteristiche[j].valori_default.replace(",", ", "));
                                if(j < categories[i].caratteristiche.length)
                                    characteristics.textContent.concat("<br>");
                            }
                            cell4.appendChild(characteristics);
                            row.appendChild(cell4);

                            var cell5 = document.createElement("td");
                            var edita = document.createElement("button");
                            edita.addEventListener("click", () => {    
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
                            row.appendChild(cell);

                            var cell2 = document.createElement("td");
                            var cancellazione = document.createElement("button");
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
                            cell2.appendChild(cancellazione);
                            row.appendChild(cell2);

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

                    } else {
                        Swal.fire({title: "Sorry", text: callStatus + ": " + callResponse, icon: "warning"});
                    }
                },
                null, null, null, bearer_token);
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
        let seeCategories = document.getElementById("categoriesView");
        if (seeCategories)
            seeCategories.addEventListener("click", function (e) {
                handleSeeCategories();
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
