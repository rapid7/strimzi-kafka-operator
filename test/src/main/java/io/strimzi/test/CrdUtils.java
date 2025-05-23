/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test;

import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Class with methods and fields useful for testing CRD related things
 */
public final class CrdUtils {
    /**
     * Path to the KafkaTopic CRD definition YAML
     */
    public static final String CRD_TOPIC = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/043-Crd-kafkatopic.yaml";

    /**
     * Path to the Kafka CRD definition YAML
     */
    public static final String CRD_KAFKA = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/040-Crd-kafka.yaml";

    /**
     * Path to the KafkaConnect CRD definition YAML
     */
    public static final String CRD_KAFKA_CONNECT = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/041-Crd-kafkaconnect.yaml";

    /**
     * Path to the KafkaUser CRD definition YAML
     */
    public static final String CRD_KAFKA_USER = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/044-Crd-kafkauser.yaml";

    /**
     * Path to the KafkaBridge CRD definition YAML
     */
    public static final String CRD_KAFKA_BRIDGE = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/046-Crd-kafkabridge.yaml";

    /**
     * Path to the KafkaMirrorMaker2 CRD definition YAML
     */
    public static final String CRD_KAFKA_MIRROR_MAKER_2 = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/048-Crd-kafkamirrormaker2.yaml";

    /**
     * Path to the KafkaConnector CRD definition YAML
     */
    public static final String CRD_KAFKA_CONNECTOR = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/047-Crd-kafkaconnector.yaml";

    /**
     * Path to the KafkaRebalance CRD definition YAML
     */
    public static final String CRD_KAFKA_REBALANCE = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/049-Crd-kafkarebalance.yaml";

    /**
     * Path to the KafkaNodePool CRD definition YAML
     */
    public static final String CRD_KAFKA_NODE_POOL = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/045-Crd-kafkanodepool.yaml";

    /**
     * Path to the StrimziPodSet CRD definition YAML
     */
    public static final String CRD_STRIMZI_POD_SET = TestUtils.USER_PATH + "/../packaging/install/cluster-operator/042-Crd-strimzipodset.yaml";

    private CrdUtils() { }

    /**
     * Creates a CRD resource in the Kubernetes cluster
     *
     * @param client    Kubernetes client
     * @param crdName   Name of the CRD
     * @param crdPath   Path to the CRD YAML
     */
    public static void createCrd(KubernetesClient client, String crdName, String crdPath)   {
        if (client.apiextensions().v1().customResourceDefinitions().withName(crdName).get() != null) {
            deleteCrd(client, crdName);
        }

        client.apiextensions().v1()
                .customResourceDefinitions()
                .load(crdPath)
                .create();
        client.apiextensions().v1()
                .customResourceDefinitions()
                .load(crdPath)
                .waitUntilCondition(CrdUtils::isCrdEstablished, 10, TimeUnit.SECONDS);
    }

    /**
     * Checks if the CRD has been established
     *
     * @param crd   The CRD resource
     *
     * @return  True if the CRD is established. False otherwise.
     */
    private static boolean isCrdEstablished(CustomResourceDefinition crd)   {
        return crd.getStatus() != null
                && crd.getStatus().getConditions() != null
                && crd.getStatus().getConditions().stream().anyMatch(c -> "Established".equals(c.getType()) && "True".equals(c.getStatus()));
    }

    /**
     * Deletes the CRD from the Kubernetes cluster
     *
     * @param client    Kubernetes client
     * @param crdName   Name of the CRD
     */
    public static void deleteCrd(KubernetesClient client, String crdName)   {
        if (client.apiextensions().v1().customResourceDefinitions().withName(crdName).get() != null) {
            client.apiextensions().v1().customResourceDefinitions().withName(crdName).withPropagationPolicy(DeletionPropagation.BACKGROUND).delete();
            client.apiextensions().v1().customResourceDefinitions().withName(crdName).waitUntilCondition(Objects::isNull, 30_000, TimeUnit.MILLISECONDS);
        }
    }
}
