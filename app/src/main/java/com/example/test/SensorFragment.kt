package com.example.test

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.*
import android.location.*
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.test.databinding.FragmentSensorBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SensorFragment : Fragment(), SensorEventListener, LocationListener {
    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private val csvData = mutableListOf<String>()
    private var isRecording = false

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sensorManager = requireActivity().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        locationManager = requireActivity().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager

        checkLocationPermission()

        binding.startButton.setOnClickListener {
            isRecording = true
            csvData.clear()
            registerSensors()
            startLocationUpdates()
        }

        binding.stopButton.setOnClickListener {
            isRecording = false
            unregisterSensors()
            locationManager.removeUpdates(this)
            saveCsvFile()
            findNavController().navigate(R.id.action_sensorFragment_to_endFragment)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun registerSensors() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun saveCsvFile() {
        val filename = "sensor_data_${System.currentTimeMillis()}.csv"
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)
        file.printWriter().use { out ->
            out.println("Time,AccelX,AccelY,AccelZ,Latitude,Longitude")
            csvData.forEach { out.println(it) }
        }
        Toast.makeText(requireContext(), "CSVを保存しました: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && isRecording) {
            val (x, y, z) = event.values
            val time = getCurrentTime()
            binding.accelText.text = "加速度\nX: $x\nY: $y\nZ: $z"
            csvData.add("$time,$x,$y,$z,,")
        }
    }

    override fun onLocationChanged(location: Location) {
        if (isRecording) {
            val lat = location.latitude
            val lon = location.longitude
            val time = getCurrentTime()
            binding.gpsText.text = "位置\n緯度: $lat\n経度: $lon"
            csvData.add("$time,,,,$lat,$lon")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

