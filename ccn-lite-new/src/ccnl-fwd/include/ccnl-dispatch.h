/**
 * @addtogroup CCNL-fwd
 * @{
 * @file ccnl-dispatch.h
 * @brief Detect packet forward and call packet specific forwarder
 *
 * @author Christopher Scherb <christopher.scherb@unibas.ch>
 * @author Christian Tschudin <christian.tschudin@unibas.ch> 
 *
 * @copyright (C) 2011-18, University of Basel
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

#ifndef CCNL_DISPATCH_H
#define CCNL_DISPATCH_H

#include "ccnl-fwd.h"

//struct ccnl_suite_s ccnl_core_suites[CCNL_SUITE_LAST];

/**
 * @brief       Initialize the dispatcher for handling different packet forwarders
 */
void
ccnl_core_init(void);


/**
 * @brief       Processing of Local RPC messages
 * 
 * @param[in] relay     pointer to current ccnl relay
 * @param[in] ifndx     index of the interface from which the data were received
 * @param[in] data      data which were received
 * @param[in] datalen   length of the received data
 * @param[in] sa        socketaddress from which the packet was received
 * @param[in] addrlen   length of the socketaddress
 */
void
ccnl_core_RX(struct ccnl_relay_s *relay, int ifndx, unsigned char *data,
             int datalen, struct sockaddr *sa, int addrlen);

#endif
/** @} */
