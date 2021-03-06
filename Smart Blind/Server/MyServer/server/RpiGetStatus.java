package edu.rit.csci759.jsonrpc.server;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import edu.rit.csci759.rspi.exercise.RpiGetStatusInterface;
import edu.rit.csci759.rspi.utils.MCP3008ADCReader;

public class RpiGetStatus implements RpiGetStatusInterface {

	public static GpioPinDigitalOutput greenPin, yellowPin, redPin;
	public static GpioController gpio;
	private static boolean keepRunning = true;
	private final boolean DEBUG = false;
	
	public RpiGetStatus() {

		gpio = GpioFactory.getInstance();
		greenPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "green",
				PinState.LOW);
		yellowPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "yellow",
				PinState.LOW);
		redPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "red",
				PinState.LOW);

		MCP3008ADCReader.initSPI(gpio);

		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run()

			{
				System.out.println("Shutting Down.");
				keepRunning = false;
			}

		});

	}
	
	

	@Override
	public void led_all_off() {
		redPin.low();
		greenPin.low();
		yellowPin.low();

	}

	@Override
	public void led_all_on() {
		redPin.high();
		greenPin.high();
		yellowPin.high();

	}

	public void led_error(int blink_count) throws InterruptedException {

		int count = 0;
		while (count < blink_count) {
			redPin.toggle();
			Thread.sleep(300);
			redPin.toggle();
			count++;
		}

		led_all_off();
	}

	@Override
	public void led_when_low() {
		
		led_all_off();
		greenPin.high();
	}

	@Override
	public void led_when_mid() {
		led_all_off();
		greenPin.high();
		yellowPin.high();
	}

	@Override
	public void led_when_high() {
		led_all_off();
		led_all_on();
	}


		public String get_Status()
		{
			String blind_status=null;
			if (redPin.isHigh() && yellowPin.isHigh() && greenPin.isHigh())
			{
				blind_status = "close"; 
			}
			else if (redPin.isLow() && yellowPin.isHigh() && greenPin.isHigh())
			{
				blind_status = "half";
				
			}
			else if (redPin.isLow() && yellowPin.isLow() && greenPin.isHigh())
			{
				blind_status = "open";
			}
			return blind_status;
					
		}
		
		public int read_ambient_light_intensity() {
			
			int ambientValue=0;
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					System.out.println("Shutting down.");
					keepRunning = false;
				}
			});


			while (keepRunning)
			{
				/*
				 * Reading ambient light from the photocell sensor using the MCP3008 ADC 
				 */
				int adc_ambient = MCP3008ADCReader.readAdc(MCP3008ADCReader.MCP3008_input_channels.CH1.ch());
				// [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				// convert in the range of 1-100
				int ambient = (int)(adc_ambient / 10.24); 
				ambientValue=ambient;
				if (DEBUG){
					System.out.println("readAdc:" + Integer.toString(adc_ambient) + 
							" (0x" + MCP3008ADCReader.lpad(Integer.toString(adc_ambient, 16).toUpperCase(), "0", 2) + 
							", 0&" + MCP3008ADCReader.lpad(Integer.toString(adc_ambient, 2), "0", 8) + ")");        
					System.out.println("Ambient:" + ambient + "/100 (" + adc_ambient + "/1024)");
				}
				try{
					Thread.sleep(500L);
				}
				catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
			
		//	return 0;
			return ambientValue;
		}

		@Override
		public float read_temperature() {
			float tempValue=0;
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				public void run()
				{
					System.out.println("Shutting down.");
					keepRunning = false;
				}
			});
			
			
			while (keepRunning)
			{
				/*
				 * Reading ambient light from the photocell sensor using the MCP3008 ADC 
				 */
				int adc_ambient = MCP3008ADCReader.readAdc(MCP3008ADCReader.MCP3008_input_channels.CH1.ch());
				// [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				// convert in the range of 1-100
				int ambient = (int)(adc_ambient / 10.24); 
				
				if (DEBUG){
					System.out.println("readAdc:" + Integer.toString(adc_ambient) + 
							" (0x" + MCP3008ADCReader.lpad(Integer.toString(adc_ambient, 16).toUpperCase(), "0", 2) + 
							", 0&" + MCP3008ADCReader.lpad(Integer.toString(adc_ambient, 2), "0", 8) + ")");        
					System.out.println("Ambient:" + ambient + "/100 (" + adc_ambient + "/1024)");
				}
				
				
				/*
				 * Reading temperature from the TMP36 sensor using the MCP3008 ADC 
				 */
				int adc_temperature = MCP3008ADCReader.readAdc(MCP3008ADCReader.MCP3008_input_channels.CH0.ch());
				// [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				// convert in the range of 1-100
				int temperature = (int)(adc_temperature / 10.24); 
				
				if (DEBUG){
					System.out.println("readAdc:" + Integer.toString(adc_temperature) + 
							" (0x" + MCP3008ADCReader.lpad(Integer.toString(adc_temperature, 16).toUpperCase(), "0", 2) + 
							", 0&" + MCP3008ADCReader.lpad(Integer.toString(adc_temperature, 2), "0", 8) + ")");        
					System.out.println("Temperature:" + temperature + "/100 (" + adc_temperature + "/1024)");
				}
				
				float tmp36_mVolts =(float) (adc_temperature * (3300.0/1024.0));
				// 10 mv per degree
		        float temp_C = (float) (((tmp36_mVolts - 100.0) / 10.0) - 40.0);
		        // convert celsius to fahrenheit
		        float temp_F = (float) ((temp_C * 9.0 / 5.0) + 32);
		        
		        System.out.println("Ambient:" + ambient + "/100; Temperature:"+temperature+"/100 => "+String.valueOf(temp_C)+"C => "+String.valueOf(temp_F)+"F");
				tempValue = temp_C;

				try { Thread.sleep(500L); } catch (InterruptedException ie) { ie.printStackTrace(); }
			}
			
			return tempValue;
		}

	}


