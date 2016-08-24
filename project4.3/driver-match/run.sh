mvn clean package
rm -rf deploy/samza/*
tar -xvf ./target/pitt_cabs-0.0.1-dist.tar.gz -C deploy/samza
hadoop fs -copyFromLocal -f target/pitt_cabs-0.0.1-dist.tar.gz /
deploy/samza/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=file://$PWD/deploy/samza/config/driver-match.properties
