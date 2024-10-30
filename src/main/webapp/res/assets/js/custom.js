//Custom functions for admin template of webshop project

function showPassword(targetID) {
    var x = document.getElementById(targetID);
  
    if (x.type === "password") {
      x.type = "text";
    } else {
      x.type = "password";
    }
  }

function showPassword(clickID, targetID) {    
    const passwordField = document.getElementById(targetID);
    const togglePassword = document.getElementById(clickID);
    if(passwordField.type === "password") {
        passwordField.type = "text";
        togglePassword.classList.remove("zmdi-lock");
        togglePassword.classList.add("zmdi-lock-open");
    } else {
        passwordField.type = "password";
        togglePassword.classList.remove("zmdi-lock-open");
        togglePassword.classList.add("zmdi-lock");
    }
}

function setAutocomplete() {
    const login = document.getElementById("loginForm");
    if(login.getAttribute("autocomplete") === "on") {
        $('#loginForm').attr('autocomplete', 'off');
    } else {
        $('#loginForm').attr('autocomplete', 'on');
    }
}


function requiredField(Field1ID, Field2ID){
    const elem1 = document.getElementById(Field1ID);
    const elem2 = document.getElementById(Field2ID);

    if(elem1.value.length > 0 || elem2.value.length > 0) {
        elem1.setAttribute('required',true);
        elem2.setAttribute('required',true);
    } else {
        elem1.removeAttribute('required');
        elem2.removeAttribute('required');
    }
}

function editProposal(event){
  event.preventDefault();
  Swal.fire({
      title: "Are you sure to edit this proposal?",
      html: "You won't be able to revert this!",
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#3085d6",
      cancelButtonColor: "#d33",
      confirmButtonText: "Yes, edit it!"
    }).then((result) => {
      if (result.isConfirmed) {
        //abilita form 
        document.getElementById("wizard_with_validation").removeAttribute("inert");
        document.getElementById("wizard_with_validation-p-0").removeAttribute("disabled");

        //sposta i dati dallo storico al form
        document.getElementById("productName").value = document.getElementById("productName2").value;
        document.getElementById("producerName").value = document.getElementById("producerName2").value;
        document.getElementById("productDescription").value = document.getElementById("productDescription2").value;
        document.getElementById("productPrice").value = document.getElementById("productPrice2").value;
        document.getElementById("url").value = document.getElementById("url2").value;
        document.getElementById("notes").value = document.getElementById("notes2").value;
      }
    });
}

function deleteImage(event, imageId, imageName){
  event.preventDefault();
  Swal.fire({
      title: "Are you sure to delete image "+imageName+"?",
      text: "You won't be able to revert this!",
      icon: "error",
      showCancelButton: true,
      confirmButtonColor: "#3085d6",
      cancelButtonColor: "#d33",
    }).then((result) => {
      if (result.isConfirmed) {
          $("#delete"+imageId).submit();
      }
    });
}

function deleteCategory(event, categoryId, categoryName){
  event.preventDefault();
  Swal.fire({
      title: "Are you sure to delete category: "+categoryName+"?",
      text: "His hildrens will be entrusted to its parent",
      icon: "error",
      showCancelButton: true,
      confirmButtonColor: "#3085d6",
      cancelButtonColor: "#d33",
    }).then((result) => {
      if (result.isConfirmed) {
          $("#delete"+categoryId).submit();
      }
    });
}

function addInput(btn) {
  var num = btn.id.replace("d","");

  var div4 = document.createElement("div");
  div4.setAttribute("class", "col-sm-4");
  div4.setAttribute("id", "a"+num);
  var div42 = document.createElement("div");
  div42.setAttribute("class", "col-sm-4");
  div42.setAttribute("id", "b"+num);
  var div43 = document.createElement("div");
  div43.setAttribute("class", "col-sm-4");
  div43.setAttribute("id", "c"+num);

  var divF = document.createElement("div");
  divF.setAttribute("class", "form-group");
  var divF2 = document.createElement("div");
  divF2.setAttribute("class", "form-group");
  var divF3 = document.createElement("div");
  divF3.setAttribute("class", "form-group");

  var x = document.createElement("input");
  x.setAttribute("type", "text");
  x.setAttribute("name", "characteristicName[]");
  x.setAttribute("class", "form-control");
  x.setAttribute("placeholder", "Name");
  x.setAttribute("required", "true");

  var y = document.createElement("input");
  y.setAttribute("type", "text");
  y.setAttribute("name", "characteristicValue[]");
  y.setAttribute("class", "form-control");
  y.setAttribute("placeholder", "Add values separated by ','");
  y.setAttribute("pattern", "([^,]*[,])*[^,][^,]*");
  y.setAttribute("required", "true");

  var z = document.createElement("button");
  z.setAttribute("class", "btn btn-warning")
  z.setAttribute("type", "button");
  z.setAttribute("onclick", "removeInput(this)");
  z.setAttribute("id", num);

  var i = document.createElement("i");
  i.setAttribute("class", "zmdi zmdi-delete");
  i.textContent = " Delete row";
  
  divF.appendChild(x);
  div4.appendChild(divF);
  divF2.appendChild(y);
  div42.appendChild(divF2);
  z.appendChild(i);
  divF3.appendChild(z);
  div43.appendChild(divF3);
  document.getElementById("parentInput").appendChild(div4);
  document.getElementById("parentInput").appendChild(div42);
  document.getElementById("parentInput").appendChild(div43);

  num = parseInt(num) + 1;
  btn.setAttribute("id", "d"+num);

}

function removeInput(btn) { 
  var num = btn.id.replace("c","");
  num = parseInt(num);

  var a = document.getElementById("a"+num);
  var b = document.getElementById("b"+num);
  var c = document.getElementById("c"+num);
  a.remove();
  b.remove();
  c.remove();
}

function toggle(source) {
  checkboxes = document.getElementsByName('check[]');
  for(var i=0, n=checkboxes.length;i<n;i++) {
    checkboxes[i].checked = source.checked;
  }
}
