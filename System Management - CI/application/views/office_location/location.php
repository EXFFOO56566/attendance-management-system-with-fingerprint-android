        <h3>Setup Your Office</h3>
        <h5>Hello <span><?php echo $first_name; ?></span>.</h5>
        <hr>
        <div id="map-canvas" style="border: 2px solid rgb(83, 188, 157);"></div>
        <textarea id="info" style="display: none;"></textarea>

        <button id="saveLocation" class="btn btn-primary" style="margin-top: 20px;margin-bottom: 20px">Save Area</button>

        <h4 style="color: red;"><b>*Note</b></h4>
        <hr>
        <p><b>This map for development only. To fix this please add <a href="https://developers.google.com/maps/documentation/javascript/error-messages?utm_source=maps_js&utm_medium=degraded&utm_campaign=billing#api-key-and-billing-errors"> Google Maps key.</a></b><br>
            To add Google API:
            <ol>
                <li>Please insert it on application/views/office_location/location.php</li>
                <li>Add key after &key -> https://maps.googleapis.com/maps/api/js?sensor=false&libraries=geometry,drawing&ext=.js&key=Add the API here</li>
            </ol>
        </p>

        </div><!--row-->

        <footer>
            <div class="col-md-12" style="text-align:center;">
                <hr>
                Copyright&copy; - <?php echo date('Y'); ?> | Create by <a
                        href="https://connectwithdev.com/">connectwithdev.com</a>
            </div>
        </footer>
    </div><!-- /container -->

    <!-- Modal -->
    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="myModalLabel">Attendance Login System</h4>
                </div>
                <div class="modal-body">
                    <h2>Version</h2>
                    <p>V3.0</p>
                    <h2>About</h2>
                    <p>Attendance login system is based on the <a
                                href="https://github.com/bcit-ci/CodeIgniter">codeigniter</a>.
                    <p>If you have question, please email me : <a
                                href="mailto:abedputra@gmail.com">abedputra@gmail.com</a><br>
                        Visit: <a href="https://connectwithdev.com/page/contact"
                                  rel="nofollow">https://connectwithdev.com/</a></p>
                    <h2>License</h2>
                    <p>The MIT License (MIT).</p>
                    <p>Copyright&copy; <?php echo date('Y'); ?>, Abed Putra.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>


        <!-- /Load Js -->
        <script src="https://cdn.jsdelivr.net/clipboard.js/1.5.12/clipboard.min.js"></script>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js" type="text/javascript"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
        <script src="<?php echo base_url() . 'public/js/main.js' ?>"></script>
        <script type="text/javascript"
            src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.4.1/js/bootstrap-datepicker.min.js"></script>

        <script src='https://maps.googleapis.com/maps/api/js?sensor=false&libraries=geometry,drawing&ext=.js&key='></script>
        <script src="<?php echo base_url().'public/js/maps.js'?>"></script>
        <script>
            var baseURL = '<?php echo base_url()?>';
        </script>
    </body>
</html>
