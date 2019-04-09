DESCRIPTION="factory-configurator-client-example"

TOOLCHAIN = "POKY-GLIBC"

LICENSE = "Apache-2.0"
LICENSE_MD5SUM = "4336ad26bb93846e47581adc44c4514d"

SOURCE_REPOSITORY = "git://git@github.com/ARMmbed/factory-configurator-client-example.git"
SOURCE_BRANCH = "master"
SRCREV = "3.0.0"
APP_NAME = "factory-configurator-client"

LIC_FILES_CHKSUM = "file://${WORKDIR}/git/${APP_NAME}/pal-platform/LICENSE;md5=${LICENSE_MD5SUM}"

# Patches for quilt goes to files directory
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "${SOURCE_REPOSITORY};branch=${SOURCE_BRANCH};protocol=ssh;name=${APP_NAME};destsuffix=git/${APP_NAME} \
          file://0002-Update-primary-and-secondary-mount-points-to-config-.patch;patchdir=../git/factory-configurator-client"

RDEPENDS_${PN} = " libstdc++ libgcc"

# Installed packages
PACKAGES = "${PN}-dbg ${PN}"
FILES_${PN} += "/opt \
                /opt/arm \
                /opt/arm/factory_configurator_client_example.elf"
FILES_${PN}-dbg += "/opt/arm/.debug \
                    /usr/src/debug/fcc"

TARGET = "Yocto_Generic_YoctoLinux_mbedtls"

# Allowed [Debug|Release]
RELEASE_TYPE="Debug"

inherit cmake

do_setup_pal_env() {
    echo "Setup pal env"
    CUR_DIR=$(pwd)
    cd "${WORKDIR}/git/${APP_NAME}"
    # Clean the old build directory
    rm -rf "__${TARGET}"
    /usr/local/bin/mbed deploy --protocol ssh
    SSH_AUTH_SOCK=${SSH_AUTH_SOCK} python ./pal-platform/pal-platform.py -v deploy --target="${TARGET}" generate
    cd ${CUR_DIR}
}

addtask setup_pal_env after do_unpack before do_patch

do_configure() {
    CUR_DIR=$(pwd)
    cd "${WORKDIR}/git/${APP_NAME}/__${TARGET}"

    # Set cmake extra defines
    EXTRA_DEFINES=""
    if [ "${RESET_STORAGE}" = "1" ]; then
        echo "Define RESET_STORAGE for cmake."
        EXTRA_DEFINES="-DRESET_STORAGE=ON"
    fi

    cmake -G "Unix Makefiles" ${EXTRA_DEFINES} -DCMAKE_BUILD_TYPE="${RELEASE_TYPE}" -DCMAKE_TOOLCHAIN_FILE="./../pal-platform/Toolchain/${TOOLCHAIN}/${TOOLCHAIN}.cmake" -DEXTARNAL_DEFINE_FILE="./../linux-config.cmake"
    cd ${CUR_DIR}
}

do_compile() {
    CUR_DIR=$(pwd)
    cd "${WORKDIR}/git/${APP_NAME}/__${TARGET}"
    make factory-configurator-client-example.elf
    cd ${CUR_DIR}
}

do_install() {
    install -d "${D}/opt/arm"
    install "${WORKDIR}/git/${APP_NAME}/__${TARGET}/${RELEASE_TYPE}/factory-configurator-client-example.elf" "${D}/opt/arm"
}
