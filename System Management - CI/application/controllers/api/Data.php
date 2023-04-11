<?php
defined('BASEPATH') OR exit('No direct script access allowed');

require APPPATH . 'libraries/REST_Controller.php';

class Data extends REST_Controller
{

    /**
     * Create a new controller instance.
     *
     * @return void
     */
    public function __construct()
    {
        // Construct the parent class
        parent::__construct();
        $this->load->model('MainModel', 'MainModel', TRUE);
        $this->load->model('ApiModel', 'ApiModel', TRUE);
    }

    /**
     * Api get location office for app.
     *
     * @return void
     */
    public function index_post()
    {
        $key = $this->input->post('key');
        $result = $this->MainModel->getSettings();
        $data['key'] = $result->key_insert;

        if (!empty($key)) {
            if ($key == $data['key']) {

                // Get location
                $dataLocation = $this->ApiModel->getLocationArea();
                // Init the location
                $data = [
                    'message' => 'success',
                    'data' => $dataLocation
                ];
                // Check if data not empty
                if (count($data['data']) > 0) {
                    $this->response($data, REST_Controller::HTTP_OK);
                } else {
                    $this->response(['message' => 'empty', 'data' => 0], REST_Controller::HTTP_OK);
                }
            } else {
                $this->response(['message' => 'Your key is wrong'], REST_Controller::HTTP_OK);
            }
        } else {
            $this->response(['message' => 'Please insert your key first!'], REST_Controller::HTTP_OK);
        }
    }

    /**
     * Api get md5 location office.
     *
     * @return void
     */
    public function getMd5Location_post()
    {

        $key = $this->input->post('key');
        $result = $this->MainModel->getSettings();
        $data['key'] = $result->key_insert;

        if (!empty($key)) {
            if ($key == $data['key']) {

                // Get md5 from database
                $hashMd5 = $this->ApiModel->getMd5Location();
                $data = [
                    'message' => 'success',
                    'data' => $hashMd5
                ];
                $this->response($data, REST_Controller::HTTP_OK);

            } else {
                $this->response(['message' => 'Your key is wrong'], REST_Controller::HTTP_OK);
            }
        } else {
            $this->response(['message' => 'Please insert your key first!'], REST_Controller::HTTP_OK);
        }
    }

    /**
     * Api store location office.
     *
     * @return void
     */
    public function storeLocation_post()
    {
        $lat = $this->input->post('lat');
        $longt = $this->input->post('longt');

        $cleanPost['lat'] = $lat;
        $cleanPost['longt'] = $longt;

        // Store the location
        $store = $this->ApiModel->insertLocation($cleanPost);
        // Check is stored
        if ($store) {
            $data = [
                'message' => 'success',
            ];
        } else {
            $data = [
                'message' => 'error',
            ];
        }
        // Return the result
        $this->response($data, REST_Controller::HTTP_OK);
    }

    /**
     * Api delete location office.
     *
     * @return void
     */
    public function deleteLocationTable_post()
    {
        // Delete the location
        $this->ApiModel->deleteTableLocation();
    }

    /**
     * Api get the location office.
     *
     * @return void
     */
    public function showAllDataLocation_get()
    {
        $data = $this->ApiModel->getLocationArea();
        // Return the json
        $this->response($data, REST_Controller::HTTP_OK);
    }

    /**
     * Api store the md5 location office.
     *
     * @return void
     */
    public function storeMd5Location_post()
    {
        $md5 = $this->input->post('md5');
        $hashMd5 = md5($md5);

        $cleanPost['md5'] = $hashMd5;
        // Store new md5
        $this->ApiModel->insertMd5($cleanPost);
    }

    /**
     * Api for attendance check-in or check-out.
     *
     * @return void
     */
    public function absent_attendance_post()
    {
        if ($this->input->server('REQUEST_METHOD') == 'POST') {

            // Get key from request
            $key = $this->input->post('key');

            // Get settings data
            $result = $this->MainModel->getSettings();
            $data['many_employee'] = $result->many_employee;
            $data['start'] = $result->start_time;
            $data['out'] = $result->out_time;
            $data['key'] = $result->key_insert;

            // Check if key not empty
            if (!empty($key)) {
                if ($key == $data['key']) {

                    $Q = $this->security->xss_clean($this->input->post('q', TRUE));
                    $name = $this->security->xss_clean($this->input->post('name', TRUE));
                    $date = $this->security->xss_clean($this->input->post('date', TRUE));
                    $location = $this->security->xss_clean($this->input->post('location', TRUE));

                    // Check command is in our out
                    if ($Q == 'in') {

                        $in_time = $this->security->xss_clean($this->input->post('in_time', TRUE));
                        $change_in_time = strtotime($in_time);

                        // Get late time
                        $get_late_time = $this->getTime($change_in_time - strtotime($data['start']));
                        $late_time = "$get_late_time[0]:$get_late_time[1]:$get_late_time[2]";

                        $allData = array(
                            'name' => $name,
                            'date' => $date,
                            'in_location' => $location,
                            'in_time' => $in_time,
                            'late_time' => $late_time
                        );

                        $insertData = $this->MainModel->insertAbsent($allData);
                        if ($insertData == true) {
                            echo 'Success!';
                        } else {
                            echo 'Error! Something Went Wrong!';
                        }
                    } else if ($Q == 'out') {

                        $out_time = $this->security->xss_clean($this->input->post('out_time', TRUE));
                        $change_out_time = strtotime($out_time);

                        // Open in_time from database
                        $getDataIn['in_time'] = $this->MainModel->getDataAbsent('name', $name, 'date', $date);
                        $get_in_database = strtotime($getDataIn['in_time']);

                        // Get work hour
                        $get_work_hour = $this->getTime($change_out_time - $get_in_database);
                        $work_hour = "$get_work_hour[0]:$get_work_hour[1]:$get_work_hour[2]";

                        // Get over time
                        $get_over_time = $this->getTime($change_out_time - strtotime($data['out']));
                        if ($get_in_database > strtotime($data['out']) || $change_out_time < strtotime($data['out'])) {
                            $over_time = '00:00:00';
                        } else {
                            $over_time = "$get_over_time[0]:$get_over_time[1]:$get_over_time[2]";
                        }

                        // Early out time
                        $get_early_out_time = $this->getTime(strtotime($data['out']) - $change_out_time);
                        if ($get_in_database > strtotime($data['out'])) {
                            $early_out_time = '00:00:00';
                        } else {
                            $early_out_time = "$get_early_out_time[0]:$get_early_out_time[1]:$get_early_out_time[2]";
                        }

                        // Add data
                        $allData = array(
                            'name' => $name,
                            'date' => $date,
                            'out_location' => $location,
                            'out_time' => $out_time,
                            'work_hour' => $work_hour,
                            'over_time' => $over_time,
                            'early_out_time' => $early_out_time
                        );

                        $updateData = $this->MainModel->updateAbsent($allData);
                        if ($updateData == true) {
                            echo 'Success!';
                        } else {
                            echo 'Error! Something Went Wrong!';
                        }
                    } else {
                        echo 'Error! Wrong Command!';
                    }
                } else {
                    echo 'The KEY is Wrong!';
                }
            } else {
                echo 'Please Setting KEY First!';
            }
        } else {
            echo "You can't access this page!";
        }
    }

    /**
     * Function get time.
     *
     * @param $total
     * @return array
     */
    public function getTime($total)
    {
        $hours = (int)($total / 3600);
        $seconds_remain = ($total - ($hours * 3600));
        $minutes = (int)($seconds_remain / 60);
        $seconds = ($seconds_remain - ($minutes * 60));
        return array($hours, $minutes, $seconds);
    }
}