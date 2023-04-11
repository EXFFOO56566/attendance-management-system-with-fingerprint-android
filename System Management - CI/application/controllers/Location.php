<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Location extends CI_Controller
{

    public $status;
    public $roles;

    /**
     * Create a new controller instance.
     *
     * @return void
     */
    public function __construct()
    {
        parent::__construct();
        $this->roles = $this->config->item('roles');
        $this->load->library('userlevel');
    }

    /**
     * View index location office page.
     *
     * @return void
     */
    public function index()
    {
        //user data from session
        $data = $this->session->userdata;
        if (empty($data)) {
            redirect(site_url() . 'main/login/');
        }

        //check user level
        if (empty($data['role'])) {
            redirect(site_url() . 'main/login/');
        }
        $dataLevel = $this->userlevel->checkLevel($data['role']);
        //check user level

        $data['title'] = 'Settings Your Office Area';

        // Load js
        $data['js_to_load'] = array(
            'office_location/index.js'
        );

        $data['data_external_js'] = array(
            'https://maps.googleapis.com/maps/api/js?sensor=false&libraries=geometry,drawing&ext=.js&key=', // Add key here Google API
        );

        if($dataLevel == 'is_admin'){
            $this->load->view('template/header', $data);
            $this->load->view('template/navbar', $data);
            $this->load->view('template/container');
            $this->load->view('office_location/index', $data);
            $this->load->view('template/footer', $data);
        }else{
            redirect(site_url() . 'main/');
        }
    }
}