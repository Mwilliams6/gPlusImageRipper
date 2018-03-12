<#import "layout/layout.ftl" as Layout/>
<@Layout.mainLayout>
<context:property-placeholder location="classpath:/application.properties" />

<script src="http://code.jquery.com/jquery-1.7.1.min.js"></script>

<div class="block">
    <div class="block-title">
        <div id="headerText"><h2>Google Plus Image Ripper</h2></div>
    </div>
    <div id="urlForm">

        <form class="form-horizontal" id="search-form">

          <label for="prefix" class="col-sm-3 control-label">GPlus User ID.</label>

          <input id="urlPath" name="url" class="form-control input-sm" value="116749500979671626219"/>
          <button type="submit" id="bth-search"
                    class="btn btn-primary btn-lg">Search
          </button>
       </form>
    </div>


</div>


<div id="feedback" style="width:80%; margin: 0 auto;">


</div>

</@Layout.mainLayout>

