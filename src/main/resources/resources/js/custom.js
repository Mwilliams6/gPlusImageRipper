var myVar;
$(document).ready(function () {
    $("#search-form").submit(function (event) {
        //stop submit the form, we will post it manually.
        event.preventDefault();
        madeAjaxCall();
    });
});

function madeAjaxCall(){
    $("#btn-search").prop("disabled", true);

    $.ajax({
        type: "post",
        url: "/ripper/initReq?input="+ $("#urlPath").val(),
        dataType: 'json',
        cache: false,
        timeout: 600000,

        success: function (data) {

            var json = "<h4>Found "+data+" albums...</h4>";
            $( "#feedback" ).append( json );
            pollForResults();
            myVar = window.setInterval(pollForResults, 2000);

            console.log("SUCCESS : ", data);
            $("#btn-search").prop("disabled", false);

        },
        error: function (e) {

            var json = "<h4>Error Ajax Response</h4>"
                + e.responseText;
            $('#feedback').html(json);

            console.log("ERROR : ", e);
            $("#btn-search").prop("disabled", false);

        }
    });
}

function pollForResults() {
    $.ajax({url: "/ripper/poller",
        success: function(result){
            console.log("Polled result: " + result);
            $( "#feedback" ).append( result );
            if (result.indexOf("XX-FINISHED-XX") !== -1)
            {
                clearInterval(myVar);
            }
        }
    });
}