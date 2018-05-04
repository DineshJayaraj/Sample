buildDeployService {
	email = "ecomm-architecture@officedepot.com"
	namespace = "eai"
	timeoutSeconds = "5"
	maxCpu = "2000m"
	maxMemory = "4Gi"
	healthUrl = "/eaiapi/health"
	buildCommands = [
		"mvn install --settings settings.xml -Dmaven.test.skip=true -B"
	]
	envs = [  
	'{"secretEnv":{"name": "JWT_SECRET_KEY", "secretKey": "JWT_SECRET_KEY", "secretName": "jwt-secret"}}'
	]
        volumes = [
                '{"configPathVolume":{"mountPath": "/eai/security/basicauth", "configName": "basicauth"}}'
           ]
	args = ["-Xmx2048m"]
	contextPath = "/"
	livenessProbeType = "process-java"
	readinessProbeType = "process-java"
	initialDelaySeconds = "30"
	skipPerformanceTest = "true"
	skipIntegrationTest = "true"
	metaData = [
        'clarityProjectId': 'PR003707',
        'teamName': 'Enterprise Integration',
        'SLA': 'Application',
        'serviceNowClass': 'Application',
        'serviceNowAppName': 'EAI',
        'serviceOwnerEmail': 'Madhuri.Margam@officedepot.com',
        'appSupportTeamEmail': 'EAIOpsSupport@officedepot.com',
        'devManagerEmail': 'Rich.Coggins@officedepot.com'
       ]
}
