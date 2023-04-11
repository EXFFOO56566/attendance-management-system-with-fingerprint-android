<div class="row" style="margin-bottom: 20px;">
    <div class="col-md-8">
        <h2>Welcome Back!</h2>
        <h5>Please Login.</h5>
    </div>
    <div class="col-md-4" style=" text-align: center; line-height: 99px;">
        <img src="<?php echo base_url(); ?>public/image/ic_launcher.png" width="80">
    </div>
</div>
<hr>
<?php $fattr = array('class' => 'form-signin');
echo form_open(site_url() . 'main/login/', $fattr); ?>
<div class="form-group">
    <?php echo form_input(array(
        'name' => 'email',
        'id' => 'email',
        'placeholder' => 'Email',
        'class' => 'form-control',
        'value' => set_value('email'))); ?>
    <?php echo form_error('email') ?>
</div>
<div class="form-group">
    <?php echo form_password(array(
        'name' => 'password',
        'id' => 'password',
        'placeholder' => 'Password',
        'class' => 'form-control',
        'value' => set_value('password'))); ?>
    <?php echo form_error('password') ?>
</div>
<?php
if ($recaptcha == 1) {
    echo '
          <div style="text-align:center;" class="form-group">
              <div style="display: inline-block;">' . $this->recaptcha->render() . '</div>
          </div>
          ';
}
echo form_submit(array('value' => 'Let me in!', 'class' => 'btn btn-lg btn-primary btn-block')); ?>
<?php echo form_close(); ?>
<br>
<p>Not registered? <a href="<?php echo site_url(); ?>main/register">Register</a></p>
<p>Forgot your password? <a href="<?php echo site_url(); ?>main/forgot">Forgot Password</a></p>

