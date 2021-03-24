# CCN-lite CORE Worker
*CCN-lite CORE Worker* is an extension of the *CORE worker* for the MACI experiment suite. CCN-lite is integrated in CORE to enable fast development of experiments using MACI.

## MACI: Headless and GUI worker

This worker is available with a gui (based on `maciresearch/core_worker-gui`) to be used during experiment development as well as a headless version (based on `maciresearch/core_worker`) for lightweight experiment runs.

### Build images
```
docker build -t umrds/ccnl_core_worker -f worker.Dockerfile .
```

## Standalone Quickstart
Although this container is intended to be used with MACI, it  as a standalone container for debugging. To work with the CORE GUI, the host needs to run an X11 server.

### Setup X11 on macOS
1. Install XQuartz, e.g. `brew cask install xquartz`
2. Configure X to allow connections from network clients (XQuartz -> Settings -> Security -> Allow Network Clients)

### Setup X11 on lightdm (Ubuntu 16)
Allow connections from network clients. Add to `/etc/lightdm/lightdm.conf`:

```
[SeatDefaults]
xserver-allow-tcp=true
```

... and restart lightdm using `sudo restart lightdm`.

### Setup X11 on gdm3 (Ubuntu 18)
Allow connections from network clients. Add to `/etc/gdm3/custom.conf`:

```
[security]
DisallowTCP=false
```

### Setup X11 on sddm (Kubuntu 18.04)
Add a new configuration file /etc/sddm.conf with content

```
[X11]
ServerArguments=-listen tcp -dpi 96
```

`ps ax | grep sddm` shows the desired Xorg option -listen tcp and the X Server is ready for incoming connections


### Start GUI

Add the remote docker host to the `xhost` access control list (`xhost +<DOCKER_HOST_IP>`) OR disable the access control list (`xhost +`).

The container can be started adding your IP to the DISPLAY variable:

```
docker run --rm --privileged -v /lib/modules:/lib/modules -it --cap-add=NET_ADMIN -e DISPLAY=<IP>:0 umrds/ccnl_core_worker
```

##### Attention: Linux sometimes uses other Display numbers than 0.
Checkout your Display using `echo $DISPLAY`

##### Hint: Docker for Mac users can use the special hostname `docker.for.mac.localhost`:
```
docker run --rm --privileged -v /lib/modules:/lib/modules -it --cap-add=NET_ADMIN -e DISPLAY=docker.for.mac.localhost:0 umrds/ccnl_core_worker
```

or set the DISPLAY variable in docker-compose.yml file to your <IP> and do `docker-compose up` to start the Core GUI

# Integrating MACI


[MACI](https://maci-research.net) is a framework for running series of experiments and integrates well with CORE running in prepared docker containers. MACI takes care of the experiment configurations and servers python scripts to workers, which then execute the respective experiment settings. 

To start, one must first start a maci instance which takes care of the experiment configuration:

```
git submodule update --init
docker-compose -f maci.yml up
```

MACI will then run a webinterface, which is used for creating experiment runs and communicating with the workers. It runs on [http://localhost:63658/](http://localhost:63658/). A non-localhost address is required to be configured at the worker nodes (depends on your network configuration.

To start the worker the `DISPLAY` variable needs to be left undefined, hence the `BACKEND` variable needs to be set:

```
docker run --rm --privileged -v /lib/modules:/lib/modules -it --cap-add=NET_ADMIN -e BACKEND=<backend-ip>:0 umrds/ccnl_core_worker
```

The backend variable can also be set in the `docker-compose.yml` file:

```yml
...
        environment:
          - CORE_PARAMS=/core_scripts/scenarios/default.xml
          # DISPLAY needs to be undefined, when connecting to a backend
          # - DISPLAY=docker.for.mac.localhost:0
          - BACKEND=docker.for.mac.localhost

```

