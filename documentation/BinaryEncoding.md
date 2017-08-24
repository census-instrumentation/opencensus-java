# BINARY FORMAT

## General Format
Each encoding will have a 1 byte version followed by the version format encoding:

`<version><version_format>`

This will allow us to, in 1 deprecation cycle to completely switch to a new format if needed.

## Version Format (version_id = 0)
The version format for the version_id = 0 is based on ideas from proto encoding. The main 
requirements are to allow adding and removing fields in less than 1 deprecation cycle. It
contains a list of repeated fields:

`<field><field>...`

### Field
Each field that we send on the wire will have the following format:

`<field_id><field_format>`

* `field_id` is a single byte.

* `field_format` must be defined for each metadata field separately, that means that for field_id
 = 0 in trace context the field_value may have a completely different representation than the 
 field_id = 0 in the server-stats metadata.

Each field is optional and MAY have defined a default value that can be used (if implementation 
needs one) when the field is missing. Fields can be repeated, e.g. StringTag in the tagging example.

### Serialization Rules
Because each field has its own format that is not generically defined we are forced to always add
new field ids at the end. The serialization MUST ensure that fields are serialized in version 
order (i.e. fields from version (i) must precede fields from version (i+1)). This ordering 
allows old decoders to ignore any new fields even if they do not know the format for that field.
Systems that receive extra fields that they cannot decode MAY pass them on when possible (by 
passing-through the whole opaque tail of bytes starting with the field id that the current 
binary does not understand).

### Deserialization Rules
Because all the fields will be decoded in the same order as they were defined/added, the 
deserialization will simply read the encoded input until the end of the input (if no new fields 
were received) or until the first unknown field_id.

### How can we add new fields?
If we follow the rules that we always append the new ids at the end of the buffer we can add up 
to 127. 

TODO(bdrutu): Decide what to do after 127: a) use varint encoding or b) just reserve 255 as a 
continuation byte.

### How can we remove a field?
We can stop sending any field at any moment and the decoders will be able to skip the missing ids
and use the default values.

### Trace Context

#### Trace-id

* `field_id` = 0
* `len` = 16

Is the ID of the whole trace forest. It is represented as a 16-bytes array,
e.g. (in hex), `4bf92f3577b34da6a3ce929d0e0e4736`. All bytes 0 is considered invalid.

#### Span-id

* `field_id` = 1
* `len` = 8

Is the ID of the caller span (parent). It is represented as a 8-bytes array,
e.g. (in hex), `00f067aa0ba902b7`. All bytes 0 is considered invalid.

#### Trace-options

* `field_id` = 2
* `len` = 1

Controls tracing options such as sampling, trace level etc. It is a 1-byte
representing a 8-bit unsigned integer. The least significant bit provides
recommendation whether the request should be traced or not (1 recommends the
request should be traced, 0 means the caller does not make a decision to trace
and the decision might be deferred). The flags are recommendations given by the
caller rather than strict rules to follow for 3 reasons:

1.  Trust and abuse.
2.  Bug in caller
3.  Different load between caller service and callee service might force callee to down sample.

The behavior of other bits is currently undefined.

#### Valid example
{0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100, 101, 
102, 103, 104, 2, 1}

This corresponds to:
* `traceId` = {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}
* `spanId` = {97, 98, 99, 100, 101, 102, 103, 104}
* `traceOptions` = 1

## Related Work
* [TraceContext Project](https://github.com/TraceContext/tracecontext-spec)
* [Stackdriver TraceContext Header](https://cloud.google.com/trace/docs/support)
* [B3 TraceContext Header](https://github.com/openzipkin/b3-propagation)