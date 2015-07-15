/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.app;

import com.playonlinux.ui.events.EventHandler;
import com.playonlinux.ui.events.EventHandlerPlayOnLinuxImplementation;
import com.playonlinux.core.injection.AbstractConfiguration;
import com.playonlinux.core.injection.Bean;
import com.playonlinux.core.scripts.InstallerSource;
import com.playonlinux.core.scripts.InstallerSourceWebserviceImplementation;
import com.playonlinux.core.scripts.ScriptFactory;
import com.playonlinux.core.scripts.ScriptFactoryDefaultImplementation;
import com.playonlinux.core.lang.LanguageBundle;
import com.playonlinux.core.lang.LanguageBundleSelector;
import com.playonlinux.core.log.LogStreamFactory;
import com.playonlinux.core.python.InterpreterFactory;
import com.playonlinux.core.python.JythonCommandInterpreterFactory;
import com.playonlinux.core.python.JythonInterpreterFactory;
import com.playonlinux.core.services.manager.PlayOnLinuxServicesManager;
import com.playonlinux.core.services.manager.ServiceInitializationException;
import com.playonlinux.core.services.manager.ServiceManager;
import com.playonlinux.ui.api.Controller;
import com.playonlinux.ui.api.CommandInterpreterFactory;
import com.playonlinux.ui.impl.cli.ControllerCLIImplementation;
import com.playonlinux.ui.impl.gtk.ControllerGTKImplementation;
import com.playonlinux.ui.impl.javafx.ControllerJavaFXImplementation;
import com.playonlinux.core.webservice.DownloadManager;
import com.playonlinux.engines.wine.WineVersionSource;
import com.playonlinux.engines.wine.WineversionsSourceWebserviceImplementation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class PlayOnLinuxConfig extends AbstractConfiguration {

    private boolean useCliInterface = false;
    private PlayOnLinuxContext playOnLinuxContext = new PlayOnLinuxContext();
    private ServiceManager playOnLinuxServiceManager = new PlayOnLinuxServicesManager();
    private boolean useGTKInterface;
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Bean
    public Controller controller() {
        if(useCliInterface) {
            return new ControllerCLIImplementation();
        } else if(useGTKInterface) {
            return new ControllerGTKImplementation();
        } else {
            return new ControllerJavaFXImplementation();
        }
    }

    @Bean
    public InstallerSource installerSource() throws MalformedURLException {
        return new InstallerSourceWebserviceImplementation(new URL(playOnLinuxContext.getProperty("webservice.url")));
    }

    @Bean
    public WineVersionSource wineVersionSource() throws MalformedURLException {
        return new WineversionsSourceWebserviceImplementation(new URL(playOnLinuxContext.getProperty("webservice.wine.url")));
    }

    @Bean
    public EventHandler eventHandler() {
            return new EventHandlerPlayOnLinuxImplementation();
    }

    @Bean
    public PlayOnLinuxContext playOnLinuxContext() throws PlayOnLinuxException, IOException {
        return playOnLinuxContext;
    }

    @Bean
    public ServiceManager playOnLinuxBackgroundServicesManager() {
        return playOnLinuxServiceManager;
    }

    @Bean
    public LanguageBundle languageBundle() {
        return LanguageBundleSelector.forLocale(Locale.getDefault());
    }

    @Bean
    public DownloadManager downloadManager() throws ServiceInitializationException {
        DownloadManager downloadManager = new DownloadManager();
        playOnLinuxServiceManager.register(downloadManager);
        return downloadManager;
    }

    @Bean
    public CommandInterpreterFactory commandInterpreterFactory() {
        return new JythonCommandInterpreterFactory(defaultExecutor());
    }

    @Bean
    public InterpreterFactory interpreterFactory() {
        return new JythonInterpreterFactory();
    }

    @Bean
    public LogStreamFactory logStreamFactory() {
        return new LogStreamFactory();
    }
    
    @Bean
    public ScriptFactory scriptFactory() {
        return new ScriptFactoryDefaultImplementation().withExecutor(defaultExecutor());
    }

    private ExecutorService defaultExecutor() {
        return executor;
    }

    @Override
    protected String definePackage() {
        return "com.playonlinux";
    }

    public void setUseCLIInterface(boolean enabled) {
        this.useCliInterface = enabled;
    }

    public void setUseGTKInterface(boolean useGTKInterface) {
        this.useGTKInterface = useGTKInterface;
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
