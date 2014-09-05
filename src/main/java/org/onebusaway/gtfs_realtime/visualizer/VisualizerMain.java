/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_realtime.visualizer;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.onebusaway.cli.CommandLineInterfaceLibrary;
import org.onebusaway.guice.jsr250.LifecycleService;

import com.dialog.googletracks.TracksServiceRequest;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class VisualizerMain {

  private static final String ARG_VEHICLE_POSITIONS_URL = "vehiclePositionsUrl";
  

  private static final String ARG_PORT_NUMBER = "port";

  private static final String ARG_TRACKS_UPDATE = "TracksUpdate";
  
  private static final String ARG_SIMULATE_TRACKS = "TracksSimulate";
  
  private int ourPort = 8080;

  public static void main(String[] args) throws Exception {
    VisualizerMain m = new VisualizerMain();
    m.run(args);
  }

  private void run(String[] args) throws Exception {

    if (args.length == 0 || CommandLineInterfaceLibrary.wantsHelp(args)) {
      printUsage();
      System.exit(-1);
    }

    Options options = new Options();
    buildOptions(options);
    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);
    
    if (cli.getOptionValue(ARG_VEHICLE_POSITIONS_URL) == null || cli.getOptionValue(ARG_VEHICLE_POSITIONS_URL).isEmpty()) {
        printUsage();
        System.exit(-1);
    }

    if (cli.getOptionValue(ARG_PORT_NUMBER) != null) {
    	try {
    		int ourPort = Integer.parseInt(cli.getOptionValue(ARG_PORT_NUMBER));
    		VisualizerServer.setPort(ourPort);
    	} catch (NumberFormatException e) {
    		System.out.println("Port number " + cli.getOptionValue(ARG_PORT_NUMBER) + " is invalid");
    	    System.exit(-1);
    	}
    }
    Set<Module> modules = new HashSet<Module>();
    VisualizerModule.addModuleAndDependencies(modules);

    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    VisualizerService service = injector.getInstance(VisualizerService.class);
    service.setVehiclePositionsUri(new URI(
        cli.getOptionValue(ARG_VEHICLE_POSITIONS_URL)));
    if (cli.hasOption(ARG_TRACKS_UPDATE)) {
    	service.setTracksUpdate(true);
    }
    if (cli.hasOption(ARG_SIMULATE_TRACKS)) {
        service.setTracksSimulate(true);
        TracksServiceRequest.setSimulateTracks(true);
    }
    VisualizerServer vs = injector.getInstance(VisualizerServer.class);

    LifecycleService lifecycleService = injector.getInstance(LifecycleService.class);
    lifecycleService.start();
  }

  private void printUsage() {
    CommandLineInterfaceLibrary.printUsage(getClass());
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_VEHICLE_POSITIONS_URL, true, "");
    options.addOption(ARG_PORT_NUMBER, true, "");
    options.addOption(ARG_TRACKS_UPDATE, false, "");
    options.addOption(ARG_SIMULATE_TRACKS, false, "");
  }
}
