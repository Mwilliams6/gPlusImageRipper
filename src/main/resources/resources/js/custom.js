var myVar;
$(document).ready(function () {
    $("#bth-cancel").prop("disabled",true);

    $('#bth-search').on('click', function(event) {
        //stop submit the form, we will post it manually.
        event.preventDefault();
        madeAjaxCall();
        $("#bth-search").prop("disabled",true);
        $("#bth-cancel").prop("disabled",false);
        $("#feedback").html("");
    });

    $('#bth-cancel').on('click', function(event) {
        //stop submit the form, we will post it manually.
        event.preventDefault();
        $("#bth-search").prop("disabled",false);
        $("#bth-cancel").prop("disabled",true);
        $( "#progressBarContainer" ).html("<div class='progress'><div class='progress-bar' role='progressbar' style='width:100%'></div></div>");
        clearInterval(myVar);

        $.ajax({url: "/ripper/cancel",
            success: function(){
                console.log("Cancelled");

            }
        });
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
                $("#bth-search").prop("disabled",false);
                clearInterval(myVar);

            }
        }
    });

    $.ajax({url: "/ripper/getProgress",
        success: function(result){
            console.log("Polled result: " + result);
            $( "#progressBarContainer" ).html(result);
        }
    });

}