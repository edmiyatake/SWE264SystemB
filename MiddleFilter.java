import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

/******************************************************************************************************************
* File:MiddleFilter.java
* Project: Lab 1
* Copyright:
*   Copyright (c) 2020 University of California, Irvine
*   Copyright (c) 2003 Carnegie Mellon University
* Versions:
*   1.1 January 2020 - Revision for SWE 264P: Distributed Software Architecture, Winter 2020, UC Irvine.
*   1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
* Parameters: None
* Internal Methods: None
******************************************************************************************************************/

public class MiddleFilter extends FilterFramework
{
	public void run()
    {
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// steal this code from the sink filter
		int MeasurementLength = 8;  // This is the length of all measurements in bytes
        int IdLength = 4;           // This is the length of IDs in the byte stream
		long measurement;           // Used to store measurement data
        int id;                     // Used to store measurement ID
        int i;                      // Loop counter

		// we want to store the previous two values 
		Queue<Double> altitudeHistory = new LinkedList<>();  // Stores the last two altitudes
        double WILD_JUMP_LIMIT = 100.0;  // Wild jump threshold in feet

		// Next we write a message to the terminal to let the world know we are alive... do we need to know?
		// System.out.print( "\n" + this.getName() + "::Middle Reading ");

		// honestly we can steal most of the code from the sink filter except updating the queue
		try (PrintWriter wildPointsWriter = new PrintWriter(new FileWriter("WildPoints.csv"))) {
            // Write the header for the wild points file
            wildPointsWriter.println("OriginalAltitude");

			while (true)
			{
				try
				{
					// Read in the ID
					id = 0;
					for (i = 0; i < IdLength; i++) {
						databyte = ReadFilterInputPort();
						id = id | (databyte & 0xFF);
						if (i != IdLength - 1) {
							id = id << 8;
						}
						WriteFilterOutputPort(databyte); // Pass ID to the output port
						bytesread++;
						byteswritten++;
					}

					// Read the measurement
					measurement = 0;
					for (i = 0; i < MeasurementLength; i++) {
						databyte = ReadFilterInputPort();
						measurement = measurement | (databyte & 0xFF);
						if (i != MeasurementLength - 1) {
							measurement = measurement << 8;
						}
						bytesread++;
					}

					// look for altitude (ID == 2)
					if (id == 2) {
						// Handle altitude data
						double currentAltitude = Double.longBitsToDouble(measurement);

						if (altitudeHistory.size() == 2) {
							// Check for wild jumps
							double prevAltitude1 = altitudeHistory.poll();
							double prevAltitude2 = altitudeHistory.peek();

							if (Math.abs(currentAltitude - prevAltitude2) > WILD_JUMP_LIMIT) {
								// Wild jump detected
								wildPointsWriter.printf("%.5f%n", currentAltitude);

								// Replace the current altitude
								currentAltitude = (altitudeHistory.size() == 1) ? prevAltitude2
										: (prevAltitude1 + prevAltitude2) / 2;
								currentAltitude = currentAltitude * -1;
							}
						}

						// Update altitude history
						altitudeHistory.offer(currentAltitude);
						if (altitudeHistory.size() > 2) {
							altitudeHistory.poll();
						}

						// Write the modified altitude to the output port
						//currentAltitude = currentAltitude * -1;
						long modifiedMeasurement = Double.doubleToLongBits(currentAltitude);
						for (i = MeasurementLength - 1; i >= 0; i--) {
							databyte = (byte) ((modifiedMeasurement >> (8 * i)) & 0xFF);
							WriteFilterOutputPort(databyte);
							byteswritten++;
						}
					}
					else {
						// Pass through non-altitude data and send it to the sink
						for (i = MeasurementLength - 1; i >= 0; i--) {
							databyte = (byte) ((measurement >> (8 * i)) & 0xFF);
							WriteFilterOutputPort(databyte);
							byteswritten++;
						}
					}
				}
				catch (EndOfStreamException e)
				{
					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
					break;
				}
			}
		}
		catch (IOException e) {
            System.err.println(e.toString());
        }
   }
}