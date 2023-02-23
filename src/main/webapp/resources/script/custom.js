/* 
 * Copyright 2015 Judge Muzinda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$('#tableList').dataTable({"aaSorting": []});
$('#userList').dataTable({"aaSorting": [], 
    "search": {
        "search": "Xxxxxxxxxx12345"
    }});

$('.itemList').dataTable({"aaSorting": []});
$("#province").change(function () {
    $this = $(this);
    if ($this.val() === "")
        return;
    $("#supportGroup").html("--Select Item--");
    $("#primaryClinic").html("--Select Item--");
    $("#district").html("<option value=''>......... loading districts </option>");
    $("#supportGroupDistrict").html("<option value=''>......... loading districts </option>");
    $.get(path + "/global/getprovincedistricts", {"province": $this.val()}, function (data) {
        $("#district").html(processDropDown(data));
        $("#supportGroupDistrict").html(processDropDown(data));
    });
});

$("#provinces").change(function () {
    $this = $(this);

    if ($this.val() === "") {
        $("#districts").find('option').remove().end().append('<option value="">--Select Item--</option>').val('');
        $("#facilities").find('option').remove().end().append('<option value="">--Select Item--</option>').val('');
        return;
    }
    $("#facilities").find('option').remove().end().append('<option value="">--Select Item--</option>').val('');
    $("#districts").find('option').remove().end().append('<option value="">.......... loading districts</option>').val('');
    $.get(path + "/global/getdistrictsinprovinces", {"provinces": normalizeArgs($this.val())}, function (data) {
        $("#districts").html(processDropDown(data));

    });
});

$("#district").change(function () {
    $this = $(this);
    if ($this.val() === "") {
        $("#primaryClinic").find('option').remove().end().append('<option value="">--Select Item--</option>').val('');
        return;
    }
    $("#primaryClinic").find('option').remove().end().append('<option value="">......... loading clinics</option>').val('');
    $.get(path + "/global/getdistrictstations", {"district": $this.val()}, function (data) {
        $("#primaryClinic").html(processDropDown(data));
    });
});
$("#districts").change(function () {

    $this = $(this);

    if ($this.val() === "") {
        $("#facilities").find('option').remove().end().append('<option value="">--Select Item--</option>').val('');
        return;
    }
    $("#facilities").find('option').remove().end().append('<option value="">......... loading clinics</option>').val('');
    $.get(path + "/global/getfacilitiesindistricts", {"districts": normalizeArgs($this.val())}, function (data) {
        $("#facilities").html(processDropDown(data));
    });
});

$("#periodType").change(function () {
    $this = $(this);
    if ($this.val() === "")
        return;
    $.get(path + "/global/getperiods", {"periodType": $this.val()}, function (data) {
        if ($this.val() === "1") {
            $("#master-content-div").html(processPeriodDropDown(data, "period", "Month"));
        } else if ($this.val() === "2") {
            $("#master-content-div").html(processPeriodDropDown(data, "quarterPeriod", "Quarters"));
        } else if ($this.val() === "3") {
            $("#master-content-div").html(processPeriodDropDown(data, "halfYearPeriod", "Half Year"));
        } else if ($this.val() === "4") {
            $("#master-content-div").html(processPeriodDropDown(data, "yearPeriod", "Year"));
        }
    });
});


$(".beneficiary").datepicker({
    changeYear: true,
    changeMonth: true,
    dateFormat: "dd/mm/yy",
    yearRange: beneficiary
});
$(".general").datepicker({
    changeYear: true,
    changeMonth: true,
    dateFormat: "dd/mm/yy",
    yearRange: general
});
$(".caregiver").datepicker({
    changeYear: true,
    changeMonth: true,
    dateFormat: "dd/mm/yy",
    yearRange: caregiver
});
$("#startDate").datepicker({
    changeYear: true,
    changeMonth: true,
    dateFormat: "dd/mm/yy"
});
$("#endDate").datepicker({
    changeYear: true,
    changeMonth: true,
    dateFormat: "dd/mm/yy"
});
$(".otherdate").datepicker({
    changeYear: true,
    changeMonth: true,
    dateFormat: "dd/mm/yy"
});



function processDropDown(items) {
    var list = "<option value=''>--Select Item--</option>";
    for (var x = 0; x < items.length; x++) {
        list += "<option value='" + items[x].id + "'>" + items[x].name + "</option>";
    }
    return list;
}
function processPeriodDropDown(items, elementName, elementLabel) {
    var str = "<label>" + elementLabel + "</label><select name='" + elementName + "' id ='" + elementName + "' class='form-control'>";
    str += processDropDown(items);
    str += "</select>";
    return str;
}

function cancelAjaxRequest(request) {
    if (request !== null)
        request.abort();
}

function monthDiff(d1, d2) {
    var months;
    months = (d2.getFullYear() - d1.getFullYear()) * 12;
    months -= d1.getMonth() + 1;
    months += d2.getMonth();
    return months <= 0 ? 0 : months;
}

// toggle side bar
$("#toggle-rem-side-bar").click(function () {
    if ($("span.toggle-span").hasClass("fa-long-arrow-left")) {
        $("span.toggle-span").removeClass("fa-long-arrow-left");
        $("span.toggle-span").addClass("fa-long-arrow-right");
        //hide side bar
        $(".sidebar-nav").addClass("custom-side-bar-ref");
        $("#page-wrapper").addClass("main-wrp");
    } else if ($("span.toggle-span").hasClass("fa-long-arrow-right")) {
        $("span.toggle-span").removeClass("fa-long-arrow-right");
        $("span.toggle-span").addClass("fa-long-arrow-left");
        //restore side bar
        $(".sidebar-nav").removeClass("custom-side-bar-ref");
        $("#page-wrapper").removeClass("main-wrp");
    }
});

$(".word-case").change(function () {
    $this = $(this);
    $this.val(jsUcfirst($this.val()));
});

function jsUcfirst(string) {
    if (string.length > 2) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
}

function gup(name, search_url) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(search_url);
    if (results == null)
        return false;
    else
        return results[1];
}

function normalizeArgs(data){
    var result=''
    data.forEach(item =>{
        result +=item+','
    });

    return result;
}



var exportDBUrl = '/zvandirireports/report/export-database/progress/index';
var exportContactsUrl = '/zvandirireports/report/export-contacts/progress/index';
var exportPatientsUrl = '/zvandirireports/report/export-patients/progress/index';
var exportCaseloadUrl = '/zvandirireports/report/export-caseload/progress/index';

function  runExportDatabaseProgress() {
        let progress=0;

        var prog = setInterval(() => {

            document.getElementById("myprogressBar").style.width = progress+"%";
            document.getElementById("myprogressBar").style.color="white";
            document.getElementById("myprogressBar").style.fontWeight="bold";
            document.getElementById("myprogressBar").style.background=
                (progress<=10)?"linear-gradient(to right, red, orange)":
                    (progress<=25)?"linear-gradient(to right, orange, gold)":
                        (progress<50)?"linear-gradient(to right, orange, gold,yellow)":
                            (progress<=70)?"linear-gradient(to right, gold, yellow, greenyellow, limegreen )":
                                (progress<85)?"linear-gradient(to right, greenyellow, limegreen)":
                                    (progress<90)?"linear-gradient(to right, limegreen,green)":
                                        "linear-gradient(to right, green, limegreen, #228B22)";
            document.getElementById("myprogressBar").innerText = (progress<=20)?progress+"%": (progress>20)?"Task is "+progress+"% complete":"";

            if(progress==100 ||progress==100.0){
                console.log('clearing interval')
                clearInterval(prog)
            }
            $.ajax({
                type: "GET",
                url: exportDBUrl,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: data=>{
                    progress=data;
                },
                error: error=>{
                    console.log(error)
                }
            });
        }, 1000)
    }



function runExportContactsProgress(){
    let progress=0;

    var contactsTask = setInterval(() => {

        document.getElementById("myprogressBar").style.width = progress+"%";
        document.getElementById("myprogressBar").style.color="white";
        document.getElementById("myprogressBar").style.fontWeight="bold";
        document.getElementById("myprogressBar").style.background=
            (progress<=10)?"linear-gradient(to right, red, orange)":
                (progress<=25)?"linear-gradient(to right, orange, gold)":
                    (progress<50)?"linear-gradient(to right, orange, gold,yellow)":
                        (progress<=70)?"linear-gradient(to right, gold, yellow, greenyellow, limegreen )":
                            (progress<85)?"linear-gradient(to right, greenyellow, limegreen)":
                                (progress<90)?"linear-gradient(to right, limegreen,green)":
                                    "linear-gradient(to right, green, limegreen, #228B22)";
        document.getElementById("myprogressBar").innerText = (progress<=20)?progress+"%": (progress>20)?"Task is "+progress+"% complete":"";

        if(progress==100 ||progress==100.0){
            clearInterval(contactsTask)
        }
        $.ajax({
            type: "GET",
            url: exportContactsUrl,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: data=>{
                progress=data;
            },
            error: error=>{
                console.log(error)
            }
        });
    }, 1000)
}


function runExportPatientsProgress(){
    let progress=0;

    var contactsTask = setInterval(() => {

        document.getElementById("myprogressBar").style.width = progress+"%";
        document.getElementById("myprogressBar").style.color="white";
        document.getElementById("myprogressBar").style.fontWeight="bold";
        document.getElementById("myprogressBar").style.background=
            (progress<=10)?"linear-gradient(to right, red, orange)":
            (progress<=25)?"linear-gradient(to right, orange, gold)":
                (progress<50)?"linear-gradient(to right, orange, gold,yellow)":
                    (progress<=70)?"linear-gradient(to right, gold, yellow, greenyellow, limegreen )":
                        (progress<85)?"linear-gradient(to right, greenyellow, limegreen)":
                            (progress<90)?"linear-gradient(to right, limegreen,green)":
                                "linear-gradient(to right, green, limegreen, #228B22)";
        document.getElementById("myprogressBar").innerText = (progress<=20)?progress+"%": (progress>20)?"Task is "+progress+"% complete":"";

        if(progress==100 ||progress==100.0){
            clearInterval(contactsTask)
        }
        $.ajax({
            type: "GET",
            url: exportPatientsUrl,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: data=>{
                progress=data;
            },
            error: error=>{
                console.log(error)
            }
        });
    }, 1000)
}


function runExportCaseloadProgress(){
    let progress=0;

    var contactsTask = setInterval(() => {

        document.getElementById("myprogressBar").style.width = progress+"%";
        document.getElementById("myprogressBar").style.color="white";
        document.getElementById("myprogressBar").style.fontWeight="bold";
        document.getElementById("myprogressBar").style.background=
            (progress<=10)?"linear-gradient(to right, red, orange)":
                (progress<=25)?"linear-gradient(to right, orange, gold)":
                    (progress<50)?"linear-gradient(to right, orange, gold,yellow)":
                        (progress<=70)?"linear-gradient(to right, gold, yellow, greenyellow, limegreen )":
                            (progress<85)?"linear-gradient(to right, greenyellow, limegreen)":
                                (progress<90)?"linear-gradient(to right, limegreen,green)":
                                    "linear-gradient(to right, green, limegreen, #228B22)";
        document.getElementById("myprogressBar").innerText = (progress<=20)?progress+"%": (progress>20)?"Task is "+progress+"% complete":"";

        if(progress==100 ||progress==100.0){
            clearInterval(contactsTask)
        }
        $.ajax({
            type: "GET",
            url: exportCaseloadUrl,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: data=>{
                progress=data;
            },
            error: error=>{
                console.log(error)
            }
        });
    }, 1000)
}
