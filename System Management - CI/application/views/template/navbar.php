<?php
//check user level
$dataLevel = $this->userlevel->checkLevel($role);
//check user level
?>
<nav class="navbar navbar-default">
    <div class="container">
        <div class="container-fluid">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                        data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="<?php echo site_url(); ?>main/"><img
                            src="<?php echo base_url(); ?>public/image/ic_launcher.png" width="25"></a>
            </div>

            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav">
                    <li><a href="<?php echo site_url(); ?>main/"><i class="fa fa-tachometer" aria-hidden="true"></i>
                            Dashboard</a></li>
                    <?php
                    if ($dataLevel === 'is_admin') {
                        echo '<li><a href="' . site_url() . 'report/"><i class="fa fa-table" aria-hidden="true"></i> Report</a></li>';
                        echo '<li><a href="' . site_url() . 'location/"><i class="fa fa-map" aria-hidden="true"></i> Office Location</a></li>';
                        echo '<li><a href="' . site_url() . 'settings/"><i class="fa fa-cog" aria-hidden="true"></i> Settings</a></li>';
                    }
                    if ($dataLevel === 'is_user') {
                        echo '<li><a href="' . site_url() . 'report/"><i class="fa fa-table" aria-hidden="true"></i> Report</a></li>';
                    }
                    if ($dataLevel === 'is_admin') {
                        echo '
                            <li class="dropdown">
                              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><i class="fa fa-users" aria-hidden="true"></i> Users <span class="caret"></span></a>
                              <ul class="dropdown-menu">
                                <li><a href="' . site_url() . 'user">Users List</a></li>
                                <li><a href="' . site_url() . 'user/add">Add User</a></li>
                              </ul>
                            </li>';
                    }
                    ?>
                    <li><a data-toggle="modal" data-target="#myModal"><i class="fa fa-question-circle"
                                                                         aria-hidden="true"></i> About</a></li>
                </ul>

                <ul class="nav navbar-nav navbar-right">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true"
                           aria-expanded="false"><i class="fa fa-user-circle"
                                                    aria-hidden="true"></i> <?php echo $first_name; ?> <span
                                    class="caret"></span></a>
                        <ul class="dropdown-menu">
                            <li><a href="<?php echo site_url(); ?>profileUser"><?php echo $email; ?></a></li>
                            <li><a href="<?php echo site_url(); ?>profileUser/edit">Edit Profile</a></li>
                            <li role="separator" class="divider"></li>
                            <li><a href="<?php echo base_url() . 'main/logout' ?>">Log Out</a></li>
                        </ul>
                    </li>
                </ul>
            </div><!-- /.navbar-collapse -->
        </div><!-- /.container-fluid -->
    </div>
</nav>
