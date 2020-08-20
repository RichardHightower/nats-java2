// Copyright 2015-2020 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.java;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeoutException;


/**
 * The Connection class is at the heart of the NATS Java client. Fundamentally a connection represents
 * a connection to the NATS server.
 *
 *
 * <p>When a connection is {@link #close() closed} the thread and socket resources are cleaned up.
 *
 * <p>All outgoing messages are sent through the connection object using one of the two
 * {@link #publish(String, byte[]) publish} methods or the {@link #request(String, byte[]) request} method.
 * When publishing you can specify a reply to subject which can be retrieved by the receiver to respond.
 * The request method will handle this behavior itself, but it relies on getting the value out of a Future
 * so may be less flexible than publish with replyTo set.
 *
 *
 *
 *
 *
 * <p><em>Note</em>: The publish methods take an array of bytes. These arrays <strong>will not be copied</strong>. This design choice
 * is based on the common case of strings or objects being converted to bytes. Once a client can be sure a message was received by
 * the NATS server it is theoretically possible to reuse that byte array, but this pattern should be treated as advanced and only used
 * after thorough testing.
 */
public interface Connection extends AutoCloseable {

    /**
     * Send a message to the specified subject. The message body <strong>will
     * not</strong> be copied. The expected usage with string content is something
     * like:
     *
     * <pre>
     * nc = Nats.connect()
     * nc.publish("destination", "message".getBytes("UTF-8"))
     * </pre>
     *
     * where the sender creates a byte array immediately before calling publish.
     *
     * See {@link #publish(String, String, byte[]) publish()} for more details on
     * publish during reconnect.
     *
     * @param subject the subject to send the message to
     * @param body the message body
     * @throws IllegalStateException if the reconnect buffer is exceeded
     */
    void publish(String subject, byte[] body);

    /**
     * Send a request to the specified subject, providing a replyTo subject. The
     * message body <strong>will not</strong> be copied. The expected usage with
     * string content is something like:
     *
     * <pre>
     * nc = Nats.connect()
     * nc.publish("destination", "reply-to", "message".getBytes("UTF-8"))
     * </pre>
     *
     * where the sender creates a byte array immediately before calling publish.
     * @param subject the subject to send the message to
     * @param replyTo the subject the receiver should send the response to
     * @param body the message body
     * @throws IllegalStateException if the reconnect buffer is exceeded
     */
    void publish(String subject, String replyTo, byte[] body);

    /**
     * Send a request. The returned future will be completed when the
     * response comes back.
     *
     * @param subject the subject for the service that will handle the request
     * @param data the content of the message
     * @return a Future for the response, which may be cancelled on error or timed out
     */
    Subscription request(String subject, byte[] data);


    /**
     * Create a synchronous subscription to the specified subject.
     *
     *
     * <p>As of 2.6.1 this method will throw an IllegalArgumentException if the subject contains whitespace.
     *
     * @param subject the subject to subscribe to
     * @return an object representing the subscription
     */
    Subscription subscribe(String subject);

    /**
     * Create a synchronous subscription to the specified subject and queue.
     *
     *
     * <p>As of 2.6.1 this method will throw an IllegalArgumentException if either string contains whitespace.
     *
     * @param subject the subject to subscribe to
     * @param queueName the queue group to join
     * @return an object representing the subscription
     */
    Subscription subscribe(String subject, String queueName);



    /**
     *
     * Send close signal.
     */
    void close();

    /**
     *
     * Send close signal and wait for close
     */
    void close(Duration duration) throws InterruptedException, TimeoutException;


    /**
     * Returns the connections current status.
     *
     * @return the connection's status
     */
    ConnectionStatus getStatus();


    /**
     * Return the list of known server urls, including additional servers discovered
     * after a connection has been established.
     *
     * @return this connection's list of known server URLs
     */
    Collection<ConnectURL> getServers();

}
