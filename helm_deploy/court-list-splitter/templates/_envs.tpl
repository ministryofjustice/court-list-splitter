    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "{{ .Values.springProfiles }}"

  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: court-list-splitter
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: access_key_id

  - name: AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: secret_access_key

  - name: AWS_SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: sqs_name

  - name: AWS_SQS_ENDPOINT_URL
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-queue-credentials
        key: sqs_id

  - name: AWS_SNS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: court-case-events-topic
        key: access_key_id

  - name: AWS_SNS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: court-case-events-topic
        key: secret_access_key

  - name: AWS_SNS_TOPIC_ARN
    valueFrom:
      secretKeyRef:
        name: court-case-events-topic
        key: topic_arn

{{- end -}}
